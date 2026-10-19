package com.app.lacasajojo.repository

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.app.lacasajojo.model.*
import com.app.lacasajojo.data.DataStoreManager
import com.app.lacasajojo.utils.hashPassword
import com.app.lacasajojo.model.EstadoPedido
import com.app.lacasajojo.model.Extra
import com.app.lacasajojo.model.ItemPedido
import com.app.lacasajojo.model.Pedido
import com.app.lacasajojo.model.Plato
import com.app.lacasajojo.model.Rol
import com.app.lacasajojo.model.TipoExtra
import com.app.lacasajojo.model.Usuario
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AppRepository(private val dataStore: DataStoreManager) {

    val usuarios = mutableStateListOf<Usuario>()
    val platos = mutableStateListOf<Plato>()
    val extras = mutableStateListOf<Extra>()
    val pedidos = mutableStateListOf<Pedido>()
    val carrito = mutableStateListOf<ItemPedido>()

    var usuarioActual by mutableStateOf<Usuario?>(null)
        private set
    var cargado by mutableStateOf(false)
        private set

    private val scope = CoroutineScope(Dispatchers.IO)

    // ------------------------------------------------------------------
    // Carga inicial
    // ------------------------------------------------------------------
    fun cargarDatos() {
        scope.launch {
            usuarios.clear(); usuarios.addAll(dataStore.getUsuarios().first())
            platos.clear(); platos.addAll(dataStore.getPlatos().first())
            extras.clear(); extras.addAll(dataStore.getExtras().first())
            pedidos.clear(); pedidos.addAll(dataStore.getPedidos().first())

            if (usuarios.isEmpty()) sembrarDatosIniciales()
            cargado = true
        }
    }

    // Cuentas de prueba y el menú de ejemplo del cliente (jueves 24 de septiembre de 2026)
    private fun sembrarDatosIniciales() {
        usuarios.add(
            Usuario(
                "Administrador",
                "admin@lacasajojo.cl",
                hashPassword("Admin123"),
                Rol.ADMIN
            )
        )
        usuarios.add(Usuario("Dueño", "dueno@lacasajojo.cl", hashPassword("Dueno123"), Rol.DUENO))

        platos.addAll(
            listOf(
                Plato(1, "Guatitas a la Jardinera", 4600),
                Plato(2, "Cerdo mongoliano", 4600),
                Plato(3, "Filete de Pollo", 4600),
                Plato(4, "Tortilla de Porotos verdes", 4600),
                Plato(5, "Pollo Asado", 4800),
                Plato(6, "Carne al jugo", 4800)
            )
        )
        extras.addAll(
            listOf(
                Extra(1, "Puré (natural)", TipoExtra.AGREGADO),
                Extra(2, "Arroz normal", TipoExtra.AGREGADO),
                Extra(3, "Arroz integral", TipoExtra.AGREGADO),
                Extra(4, "Verduras salteadas", TipoExtra.AGREGADO),
                Extra(5, "Fideos blancos", TipoExtra.AGREGADO),
                Extra(6, "Fideos al pesto", TipoExtra.AGREGADO),
                Extra(7, "Mix de ensaladas", TipoExtra.ENSALADA)
            )
        )
        guardarUsuarios(); guardarPlatos(); guardarExtras()
    }

    // ------------------------------------------------------------------
    // Sesión y usuarios
    // ------------------------------------------------------------------

    // Devuelve null si el ingreso fue exitoso, o el mensaje de error
    fun login(email: String, password: String): String? {
        val hash = hashPassword(password)
        val user = usuarios.find {
            it.email.equals(email.trim(), ignoreCase = true) && it.passwordHash == hash
        } ?: return "Correo o contraseña incorrectos"
        if (!user.activo) return "Tu cuenta está bloqueada. Contacta al administrador"
        usuarioActual = user
        return null
    }

    fun logout() {
        usuarioActual = null
        carrito.clear()
    }

    private fun crearUsuario(nombre: String, email: String, password: String, rol: Rol, telefono: String? = null): Boolean {
        if (usuarios.any { it.email.equals(email.trim(), ignoreCase = true) }) return false
        usuarios.add(
            Usuario(
                nombre.trim(),
                email.trim(),
                hashPassword(password),
                rol,
                telefono = telefono
            )
        )
        guardarUsuarios()
        return true
    }

    // RF-01: el registro público solo crea clientes
    fun registrarCliente(nombre: String, email: String, telefono: String, password: String): Boolean =
        crearUsuario(nombre, email, password, Rol.CLIENTE, telefono.trim())

    // Solo el administrador crea cuentas de dueño
    fun crearCuentaDueno(nombre: String, email: String, password: String): Boolean {
        if (usuarioActual?.rol != Rol.ADMIN) return false
        return crearUsuario(nombre, email, password, Rol.DUENO)
    }

    fun cambiarEstadoUsuario(email: String) {
        if (usuarioActual?.rol != Rol.ADMIN) return
        val i = usuarios.indexOfFirst { it.email == email }
        if (i >= 0) {
            usuarios[i] = usuarios[i].copy(activo = !usuarios[i].activo)
            guardarUsuarios()
        }
    }

    // ------------------------------------------------------------------
    // Menú (dueño)
    // ------------------------------------------------------------------

    // id = 0 -> plato nuevo
    fun guardarPlato(plato: Plato) {
        if (plato.id == 0) {
            val nuevoId = (platos.maxOfOrNull { it.id } ?: 0) + 1
            platos.add(plato.copy(id = nuevoId))
        } else {
            val i = platos.indexOfFirst { it.id == plato.id }
            if (i >= 0) platos[i] = plato
        }
        guardarPlatos()
    }

    fun eliminarPlato(id: Int) {
        platos.removeAll { it.id == id }
        guardarPlatos()
    }

    fun cambiarDisponiblePlato(id: Int) {
        val i = platos.indexOfFirst { it.id == id }
        if (i >= 0) {
            platos[i] = platos[i].copy(disponible = !platos[i].disponible)
            guardarPlatos()
        }
    }

    fun agregarExtra(nombre: String, tipo: TipoExtra) {
        val nuevoId = (extras.maxOfOrNull { it.id } ?: 0) + 1
        extras.add(Extra(nuevoId, nombre, tipo))
        guardarExtras()
    }

    fun eliminarExtra(id: Int) {
        extras.removeAll { it.id == id }
        guardarExtras()
    }

    fun cambiarDisponibleExtra(id: Int) {
        val i = extras.indexOfFirst { it.id == id }
        if (i >= 0) {
            extras[i] = extras[i].copy(disponible = !extras[i].disponible)
            guardarExtras()
        }
    }

    // ------------------------------------------------------------------
    // Carrito y pedidos (cliente)
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

    // Genera el pedido y su boleta. Devuelve null si no se pudo confirmar
    fun confirmarPedido(observaciones: String): Pedido? {
        val cliente = usuarioActual ?: return null
        if (cliente.rol != Rol.CLIENTE || carrito.isEmpty()) return null

        val pedido = Pedido(
            numero = (pedidos.maxOfOrNull { it.numero } ?: 0) + 1,
            emailCliente = cliente.email,
            nombreCliente = cliente.nombre,
            fecha = SimpleDateFormat(
                "dd-MM-yyyy HH:mm",
                Locale.forLanguageTag("es-CL")
            ).format(Date()),
            items = carrito.toList(),
            observaciones = observaciones.trim(),
            estado = EstadoPedido.RECIBIDO
        )
        pedidos.add(pedido)
        carrito.clear()
        guardarPedidos()
        return pedido
    }

    fun pedidosDelCliente(): List<Pedido> {
        val email = usuarioActual?.email ?: return emptyList()
        return pedidos.filter { it.emailCliente == email }.sortedByDescending { it.numero }
    }

    fun pedidoPorNumero(numero: Int): Pedido? = pedidos.find { it.numero == numero }

    fun cambiarEstadoPedido(numero: Int, nuevo: EstadoPedido) {
        if (usuarioActual?.rol != Rol.DUENO) return
        val i = pedidos.indexOfFirst { it.numero == numero }
        if (i >= 0) {
            pedidos[i] = pedidos[i].copy(estado = nuevo)
            guardarPedidos()
        }
    }

    // ------------------------------------------------------------------
    // Persistencia (DataStore)
    // ------------------------------------------------------------------
    private fun guardarUsuarios() {
        val copia = usuarios.toList()
        scope.launch { dataStore.saveUsuarios(copia) }
    }

    private fun guardarPlatos() {
        val copia = platos.toList()
        scope.launch { dataStore.savePlatos(copia) }
    }

    private fun guardarExtras() {
        val copia = extras.toList()
        scope.launch { dataStore.saveExtras(copia) }
    }

    private fun guardarPedidos() {
        val copia = pedidos.toList()
        scope.launch { dataStore.savePedidos(copia) }
    }
}
