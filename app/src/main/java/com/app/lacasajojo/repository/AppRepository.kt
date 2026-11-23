package com.app.lacasajojo.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.app.lacasajojo.data.DataStoreManager
import com.app.lacasajojo.model.EstadoPedido
import com.app.lacasajojo.model.Extra
import com.app.lacasajojo.model.ItemPedido
import com.app.lacasajojo.model.Pedido
import com.app.lacasajojo.model.Plato
import com.app.lacasajojo.model.Rol
import com.app.lacasajojo.model.TipoExtra
import com.app.lacasajojo.model.Usuario
import com.app.lacasajojo.remote.BackendApi
import com.app.lacasajojo.remote.LoginRequest
import com.app.lacasajojo.utils.hashPassword
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MultipartBody
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.Response

const val MSG_SIN_CONEXION = "Sin conexión con el servidor. Revisa que los microservicios estén encendidos"
const val MSG_CORREO_DUPLICADO = "Ya existe una cuenta con ese correo"
const val MSG_CUENTA_BLOQUEADA = "Tu cuenta está bloqueada. Contacta al administrador"
const val MSG_CREDENCIALES = "Correo o contraseña incorrectos"
const val MSG_SIN_PERMISO = "No tienes permiso para realizar esta acción"

/**
 * Fuente única de datos de la app.
 * - Los datos viven en los microservicios Spring Boot (usuarios, menú y pedidos).
 * - Las listas observables (usuarios, platos, extras, pedidos) son el estado que ven los ViewModel.
 * - DataStore guarda una copia del menú para mostrarlo rápido al abrir la app.
 * - El carrito es local: solo se envía al servidor al confirmar el pedido.
 */
class AppRepository(
    private val dataStore: DataStoreManager,
    private val remoto: BackendApi,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {

    val usuarios = mutableStateListOf<Usuario>()
    val platos = mutableStateListOf<Plato>()
    val extras = mutableStateListOf<Extra>()
    val pedidos = mutableStateListOf<Pedido>()
    val carrito = mutableStateListOf<ItemPedido>()

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set
    var cargado by mutableStateOf(false)
        private set
    var servidorDisponible by mutableStateOf(true)
        private set

    // Aviso para el usuario cuando una operación contra el servidor falla (se muestra como Toast)
    var mensajeRed by mutableStateOf<String?>(null)
        private set

    // Evita que la actualización periódica pise una operación en curso
    private val candado = Mutex()

    fun limpiarMensajeRed() {
        mensajeRed = null
    }

    // ------------------------------------------------------------------
    // Carga y sincronización
    // ------------------------------------------------------------------
    fun cargarDatos() {
        scope.launch { cargarDatosIniciales() }
    }

    // Primero la copia local del menú (rápida) y luego lo que diga el servidor
    suspend fun cargarDatosIniciales() {
        reemplazar(platos, dataStore.getPlatos().first())
        reemplazar(extras, dataStore.getExtras().first())
        refrescar()
        cargado = true
    }

    // Vuelve a pedir los datos al servidor. Devuelve false si no hubo conexión
    suspend fun refrescar(): Boolean = candado.withLock {
        try {
            val nuevosPlatos = remoto.menu.listarPlatos()
            val nuevosExtras = remoto.menu.listarExtras()
            reemplazar(platos, nuevosPlatos)
            reemplazar(extras, nuevosExtras)

            val usuario = usuarioActual
            when {
                usuario == null -> Unit
                usuario.rol == Rol.ADMIN -> reemplazar(usuarios, remoto.usuarios.listar(usuario.email))
                usuario.rol == Rol.DUENO -> reemplazar(pedidos, remoto.pedidos.listar(null))
                else -> reemplazar(pedidos, remoto.pedidos.listar(usuario.email))
            }
            dataStore.savePlatos(nuevosPlatos)
            dataStore.saveExtras(nuevosExtras)
            servidorDisponible = true
            true
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            servidorDisponible = false
            false
        }
    }

    // Actualización periódica: el dueño ve pedidos nuevos y el cliente ve el cambio de estado
    fun iniciarSincronizacion(intervaloMs: Long = 10_000) {
        scope.launch {
            while (isActive) {
                delay(intervaloMs)
                if (usuarioActual != null) refrescar()
            }
        }
    }

    private fun <T> reemplazar(lista: SnapshotStateList<T>, nuevos: List<T>) {
        Snapshot.withMutableSnapshot {
            lista.clear()
            lista.addAll(nuevos)
        }
    }

    // Llama al servidor; si no hay conexión devuelve null y avisa (salvo que avisar = false)
    private suspend fun <T> llamar(avisar: Boolean = true, bloque: suspend () -> Response<T>): Response<T>? {
        return try {
            bloque().also { servidorDisponible = true }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            servidorDisponible = false
            if (avisar) mensajeRed = MSG_SIN_CONEXION
            null
        }
    }

    // true si la respuesta fue exitosa; si no, deja un aviso para el usuario
    private fun <T> Response<T>.exitosa(accion: String): Boolean {
        if (isSuccessful) return true
        mensajeRed = "No se pudo $accion (código ${code()})"
        return false
    }

    private suspend fun <T> exclusivo(bloque: suspend () -> T): T = candado.withLock { bloque() }

    // ------------------------------------------------------------------
    // Sesión y usuarios
    // ------------------------------------------------------------------

    // Devuelve null si el ingreso fue exitoso, o el mensaje de error
    suspend fun login(email: String, password: String): String? {
        val respuesta = llamar(avisar = false) {
            remoto.usuarios.login(LoginRequest(email.trim(), hashPassword(password)))
        } ?: return MSG_SIN_CONEXION

        return when {
            respuesta.isSuccessful -> {
                val usuario = respuesta.body() ?: return "Respuesta inválida del servidor"
                if (!usuario.activo) return MSG_CUENTA_BLOQUEADA
                usuarioActual = usuario
                scope.launch { refrescar() }
                null
            }
            respuesta.code() == 401 -> MSG_CREDENCIALES
            respuesta.code() == 403 -> MSG_CUENTA_BLOQUEADA
            else -> "Error del servidor (código ${respuesta.code()})"
        }
    }

    fun logout() {
        usuarioActual = null
        carrito.clear()
        pedidos.clear()
        usuarios.clear()
    }

    // Devuelve null si se creó la cuenta, o el mensaje de error
    private suspend fun crearUsuario(
        nombre: String,
        email: String,
        password: String,
        rol: Rol,
        telefono: String? = null
    ): String? = exclusivo {
        val nuevo = Usuario(nombre.trim(), email.trim(), hashPassword(password), rol, telefono = telefono)
        val respuesta = llamar(avisar = false) {
            remoto.usuarios.crear(nuevo, usuarioActual?.email)
        } ?: return@exclusivo MSG_SIN_CONEXION

        when {
            respuesta.isSuccessful -> {
                if (usuarioActual?.rol == Rol.ADMIN) usuarios.add(respuesta.body() ?: nuevo)
                null
            }
            respuesta.code() == 409 -> MSG_CORREO_DUPLICADO
            respuesta.code() == 403 -> MSG_SIN_PERMISO
            else -> "Error del servidor (código ${respuesta.code()})"
        }
    }

    // RF-01: el registro público solo crea clientes
    suspend fun registrarCliente(nombre: String, email: String, telefono: String, password: String): String? =
        crearUsuario(nombre, email, password, Rol.CLIENTE, telefono.trim())

    // Solo el administrador crea cuentas de dueño
    suspend fun crearCuentaDueno(nombre: String, email: String, password: String): String? {
        if (usuarioActual?.rol != Rol.ADMIN) return MSG_SIN_PERMISO
        return crearUsuario(nombre, email, password, Rol.DUENO)
    }

    suspend fun cambiarEstadoUsuario(email: String) {
        if (usuarioActual?.rol != Rol.ADMIN) return
        exclusivo<Unit> {
            val actual = usuarios.find { it.email == email } ?: return@exclusivo
            val respuesta = llamar { remoto.usuarios.cambiarEstado(email, !actual.activo, usuarioActual?.email) }
                ?: return@exclusivo
            if (respuesta.exitosa("cambiar el estado del usuario")) {
                val i = usuarios.indexOfFirst { it.email == email }
                if (i >= 0) usuarios[i] = respuesta.body() ?: actual.copy(activo = !actual.activo)
            }
        }
    }

    // ------------------------------------------------------------------
    // Menú (dueño). Plato con id = 0 -> plato nuevo. Devuelve true si se guardó
    // ------------------------------------------------------------------
    suspend fun guardarPlato(plato: Plato): Boolean = exclusivo {
        // Si es una foto recién tomada/elegida, subir el JPG al microservicio.
        // La base de datos nunca debe recibir una ruta privada /data/user/0 del teléfono.
        val paraServidor = if (plato.foto != null && !plato.foto.startsWith("/api/fotos/")) {
            val archivo = File(plato.foto)
            if (!archivo.isFile) {
                mensajeRed = "No se encontró la foto del plato. Vuelve a seleccionarla."
                return@exclusivo false
            }
            val parte = MultipartBody.Part.createFormData(
                "archivo", archivo.name, archivo.asRequestBody("image/jpeg".toMediaType())
            )
            val subida = llamar { remoto.menu.subirFoto(parte) } ?: return@exclusivo false
            if (!subida.exitosa("subir la foto")) return@exclusivo false
            val rutaServidor = subida.body()?.ruta
            if (rutaServidor.isNullOrBlank() || !rutaServidor.startsWith("/api/fotos/")) {
                mensajeRed = "El servidor no devolvió una ruta válida para la imagen"
                return@exclusivo false
            }
            plato.copy(foto = rutaServidor)
        } else plato

        val respuesta = (
                if (paraServidor.id == 0) llamar { remoto.menu.crearPlato(paraServidor) }
                else llamar { remoto.menu.actualizarPlato(paraServidor.id, paraServidor) }
                ) ?: return@exclusivo false

        if (!respuesta.exitosa("guardar el plato")) return@exclusivo false
        val guardado = respuesta.body() ?: return@exclusivo false
        val i = platos.indexOfFirst { it.id == guardado.id }
        if (i >= 0) platos[i] = guardado else platos.add(guardado)
        dataStore.savePlatos(platos.toList())
        true
    }

    suspend fun eliminarPlato(id: Int): Boolean = exclusivo {
        val respuesta = llamar { remoto.menu.eliminarPlato(id) } ?: return@exclusivo false
        if (!respuesta.exitosa("eliminar el plato")) return@exclusivo false
        platos.removeAll { it.id == id }
        dataStore.savePlatos(platos.toList())
        true
    }

    suspend fun cambiarDisponiblePlato(id: Int): Boolean {
        val plato = platos.find { it.id == id } ?: return false
        return guardarPlato(plato.copy(disponible = !plato.disponible))
    }

    suspend fun agregarExtra(nombre: String, tipo: TipoExtra): Boolean = exclusivo {
        val respuesta = llamar { remoto.menu.crearExtra(Extra(0, nombre.trim(), tipo)) } ?: return@exclusivo false
        if (!respuesta.exitosa("agregar el ítem")) return@exclusivo false
        val creado = respuesta.body() ?: return@exclusivo false
        extras.add(creado)
        dataStore.saveExtras(extras.toList())
        true
    }

    suspend fun eliminarExtra(id: Int): Boolean = exclusivo {
        val respuesta = llamar { remoto.menu.eliminarExtra(id) } ?: return@exclusivo false
        if (!respuesta.exitosa("eliminar el ítem")) return@exclusivo false
        extras.removeAll { it.id == id }
        dataStore.saveExtras(extras.toList())
        true
    }

    suspend fun cambiarDisponibleExtra(id: Int): Boolean = exclusivo {
        val extra = extras.find { it.id == id } ?: return@exclusivo false
        val respuesta = llamar { remoto.menu.actualizarExtra(id, extra.copy(disponible = !extra.disponible)) }
            ?: return@exclusivo false
        if (!respuesta.exitosa("actualizar el ítem")) return@exclusivo false
        val i = extras.indexOfFirst { it.id == id }
        if (i >= 0) extras[i] = respuesta.body() ?: extra.copy(disponible = !extra.disponible)
        dataStore.saveExtras(extras.toList())
        true
    }

    // ------------------------------------------------------------------
    // Carrito (local) y pedidos (servidor)
    // ------------------------------------------------------------------
    fun agregarAlCarrito(item: ItemPedido) {
        carrito.add(item)
    }

    fun quitarDelCarrito(index: Int) {
        if (index in carrito.indices) carrito.removeAt(index)
    }

    fun cambiarCantidad(index: Int, delta: Int) {
        if (index !in carrito.indices) return
        val nueva = carrito[index].cantidad + delta
        if (nueva >= 1) carrito[index] = carrito[index].copy(cantidad = nueva)
    }

    // Envía el pedido al servidor, que le asigna el n° de boleta. Devuelve null si no se pudo confirmar
    suspend fun confirmarPedido(observaciones: String): Pedido? = exclusivo {
        val cliente = usuarioActual ?: return@exclusivo null
        if (cliente.rol != Rol.CLIENTE || carrito.isEmpty()) return@exclusivo null

        val nuevo = Pedido(
            numero = 0,
            emailCliente = cliente.email,
            nombreCliente = cliente.nombre,
            fecha = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date()),
            items = carrito.toList(),
            observaciones = observaciones.trim(),
            estado = EstadoPedido.RECIBIDO
        )
        val respuesta = llamar { remoto.pedidos.crear(nuevo) } ?: return@exclusivo null
        if (!respuesta.exitosa("confirmar el pedido")) return@exclusivo null
        val creado = respuesta.body() ?: return@exclusivo null

        pedidos.add(creado)
        carrito.clear()
        creado
    }

    fun pedidosDelCliente(): List<Pedido> {
        val email = usuarioActual?.email ?: return emptyList()
        return pedidos.filter { it.emailCliente == email }.sortedByDescending { it.numero }
    }

    fun pedidoPorNumero(numero: Int): Pedido? = pedidos.find { it.numero == numero }

    suspend fun cambiarEstadoPedido(numero: Int, nuevo: EstadoPedido): Boolean {
        if (usuarioActual?.rol != Rol.DUENO) return false
        return exclusivo {
            val respuesta = llamar { remoto.pedidos.cambiarEstado(numero, nuevo) } ?: return@exclusivo false
            if (!respuesta.exitosa("cambiar el estado del pedido")) return@exclusivo false
            val i = pedidos.indexOfFirst { it.numero == numero }
            if (i >= 0) pedidos[i] = respuesta.body() ?: pedidos[i].copy(estado = nuevo)
            true
        }
    }
}
