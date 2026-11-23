package com.app.lacasajojo.repository

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
import com.app.lacasajojo.remote.FotoSubidaResponse
import com.app.lacasajojo.remote.MenuApi
import com.app.lacasajojo.remote.PedidoApi
import com.app.lacasajojo.remote.UsuarioApi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import java.io.File
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class AppRepositoryTest {

    private val usuariosApi = mockk<UsuarioApi>()
    private val menuApi = mockk<MenuApi>()
    private val pedidosApi = mockk<PedidoApi>()
    private val dataStore = mockk<DataStoreManager>(relaxed = true)

    private val admin = Usuario("Administrador", "admin@lacasajojo.cl", "hash", Rol.ADMIN)
    private val dueno = Usuario("Dueño", "dueno@lacasajojo.cl", "hash", Rol.DUENO)
    private val cliente = Usuario("Ana", "ana@correo.cl", "hash", Rol.CLIENTE, telefono = "912345678")

    private val pollo = Plato(1, "Pollo Asado", 4800)
    private val arroz = Extra(1, "Arroz normal", TipoExtra.AGREGADO)

    private lateinit var scope: TestScope
    private lateinit var repo: AppRepository

    @Before
    fun preparar() {
        scope = TestScope(UnconfinedTestDispatcher())
        coEvery { menuApi.listarPlatos() } returns listOf(pollo)
        coEvery { menuApi.listarExtras() } returns listOf(arroz)
        coEvery { usuariosApi.listar(any()) } returns listOf(admin, dueno)
        coEvery { pedidosApi.listar(any()) } returns emptyList()
        repo = AppRepository(dataStore, BackendApi(usuariosApi, menuApi, pedidosApi), scope)
    }

    private suspend fun ingresarComo(usuario: Usuario) {
        coEvery { usuariosApi.login(any()) } returns Response.success(usuario)
        assertNull(repo.login(usuario.email, "Clave123"))
    }

    // ---------------------------------------------------------------- carga
    @Test
    fun cargarDatosUsaLaCopiaLocalYLuegoElServidor() = runTest {
        coEvery { dataStore.getPlatos() } returns flowOf(listOf(Plato(9, "Antiguo", 1000)))
        coEvery { dataStore.getExtras() } returns flowOf(emptyList())

        repo.cargarDatosIniciales()

        assertTrue(repo.cargado)
        assertTrue(repo.servidorDisponible)
        assertEquals(listOf(pollo), repo.platos.toList())
        assertEquals(listOf(arroz), repo.extras.toList())
        coVerify { dataStore.savePlatos(listOf(pollo)) }
    }

    @Test
    fun sinServidorSeMantieneLaCopiaLocal() = runTest {
        coEvery { dataStore.getPlatos() } returns flowOf(listOf(pollo))
        coEvery { dataStore.getExtras() } returns flowOf(listOf(arroz))
        coEvery { menuApi.listarPlatos() } throws IOException("sin red")

        repo.cargarDatosIniciales()

        assertTrue(repo.cargado)
        assertFalse(repo.servidorDisponible)
        assertEquals(1, repo.platos.size)
    }

    @Test
    fun cargarDatosEnSegundoPlanoTerminaDeCargar() {
        coEvery { dataStore.getPlatos() } returns flowOf(emptyList())
        coEvery { dataStore.getExtras() } returns flowOf(emptyList())
        repo.cargarDatos()
        scope.testScheduler.advanceUntilIdle()
        assertTrue(repo.cargado)
    }

    @Test
    fun refrescarComoAdminTraeLosUsuarios() = runTest {
        ingresarComo(admin)
        repo.refrescar()
        assertEquals(2, repo.usuarios.size)
    }

    @Test
    fun refrescarComoDuenoTraeTodosLosPedidosYComoClienteSoloLosSuyos() = runTest {
        val p = Pedido(1, "ana@correo.cl", "Ana", "hoy", emptyList(), "", EstadoPedido.RECIBIDO)
        coEvery { pedidosApi.listar(null) } returns listOf(p, p.copy(numero = 2, emailCliente = "otro@correo.cl"))
        coEvery { pedidosApi.listar("ana@correo.cl") } returns listOf(p)

        ingresarComo(dueno)
        repo.refrescar()
        assertEquals(2, repo.pedidos.size)

        repo.logout()
        ingresarComo(cliente)
        repo.refrescar()
        assertEquals(1, repo.pedidos.size)
    }

    @Test
    fun sincronizacionPeriodicaRefrescaSoloConSesion() = runTest {
        val bg = TestScope(StandardTestDispatcher())
        val r = AppRepository(dataStore, BackendApi(usuariosApi, menuApi, pedidosApi), bg)
        coEvery { usuariosApi.login(any()) } returns Response.success(dueno)
        r.login("dueno@lacasajojo.cl", "Dueno123")
        r.iniciarSincronizacion(1_000)
        bg.testScheduler.advanceTimeBy(2_500)
        bg.testScheduler.runCurrent()
        coVerify(atLeast = 2) { menuApi.listarPlatos() }
        bg.cancel()
    }

    // ---------------------------------------------------------------- login
    @Test
    fun loginCorrectoGuardaElUsuarioActual() = runTest {
        ingresarComo(cliente)
        assertEquals(cliente, repo.usuarioActual)
    }

    @Test
    fun loginConCredencialesIncorrectas() = runTest {
        coEvery { usuariosApi.login(any()) } returns Response.error(401, "".toResponseBody(null))
        assertEquals(MSG_CREDENCIALES, repo.login("x@x.cl", "mala"))
        assertNull(repo.usuarioActual)
    }

    @Test
    fun loginDeCuentaBloqueadaPorServidor() = runTest {
        coEvery { usuariosApi.login(any()) } returns Response.error(403, "".toResponseBody(null))
        assertEquals(MSG_CUENTA_BLOQUEADA, repo.login("x@x.cl", "Clave123"))
    }

    @Test
    fun loginDeUsuarioInactivoNoEntra() = runTest {
        coEvery { usuariosApi.login(any()) } returns Response.success(cliente.copy(activo = false))
        assertEquals(MSG_CUENTA_BLOQUEADA, repo.login(cliente.email, "Clave123"))
        assertNull(repo.usuarioActual)
    }

    @Test
    fun loginConErrorDelServidor() = runTest {
        coEvery { usuariosApi.login(any()) } returns Response.error(500, "".toResponseBody(null))
        assertEquals("Error del servidor (código 500)", repo.login("x@x.cl", "Clave123"))
    }

    @Test
    fun loginSinRespuestaCuerpoVacio() = runTest {
        coEvery { usuariosApi.login(any()) } returns Response.success<Usuario>(null)
        assertEquals("Respuesta inválida del servidor", repo.login("x@x.cl", "Clave123"))
    }

    @Test
    fun loginSinConexion() = runTest {
        coEvery { usuariosApi.login(any()) } throws IOException("sin red")
        assertEquals(MSG_SIN_CONEXION, repo.login("x@x.cl", "Clave123"))
        assertFalse(repo.servidorDisponible)
    }

    @Test
    fun logoutLimpiaSesionCarritoYListas() = runTest {
        ingresarComo(cliente)
        repo.agregarAlCarrito(ItemPedido("Pollo", 4800, listOf("Arroz"), "Mix", 1))
        repo.logout()
        assertNull(repo.usuarioActual)
        assertTrue(repo.carrito.isEmpty())
        assertTrue(repo.pedidos.isEmpty())
    }

    // ---------------------------------------------------------------- usuarios
    @Test
    fun registrarClienteOk() = runTest {
        coEvery { usuariosApi.crear(any(), any()) } returns Response.success(201, cliente)
        assertNull(repo.registrarCliente("Ana", "ana@correo.cl", "912345678", "Clave123"))
        coVerify { usuariosApi.crear(match { it.rol == Rol.CLIENTE && it.passwordHash != "Clave123" }, null) }
    }

    @Test
    fun registrarClienteConCorreoRepetido() = runTest {
        coEvery { usuariosApi.crear(any(), any()) } returns Response.error(409, "".toResponseBody(null))
        assertEquals(MSG_CORREO_DUPLICADO, repo.registrarCliente("Ana", "ana@correo.cl", "912345678", "Clave123"))
    }

    @Test
    fun registrarClienteSinPermisoOConError() = runTest {
        coEvery { usuariosApi.crear(any(), any()) } returns Response.error(403, "".toResponseBody(null))
        assertEquals(MSG_SIN_PERMISO, repo.registrarCliente("Ana", "ana@correo.cl", "912345678", "Clave123"))
        coEvery { usuariosApi.crear(any(), any()) } returns Response.error(500, "".toResponseBody(null))
        assertEquals("Error del servidor (código 500)", repo.registrarCliente("Ana", "ana@correo.cl", "912345678", "Clave123"))
    }

    @Test
    fun registrarClienteSinConexion() = runTest {
        coEvery { usuariosApi.crear(any(), any()) } throws IOException()
        assertEquals(MSG_SIN_CONEXION, repo.registrarCliente("Ana", "ana@correo.cl", "912345678", "Clave123"))
    }

    @Test
    fun soloElAdminCreaDuenos() = runTest {
        assertEquals(MSG_SIN_PERMISO, repo.crearCuentaDueno("Pedro", "pedro@correo.cl", "Clave123"))

        ingresarComo(admin)
        coEvery { usuariosApi.crear(any(), any()) } returns Response.success(201, dueno)
        assertNull(repo.crearCuentaDueno("Dueño", "dueno@lacasajojo.cl", "Clave123"))
        coVerify { usuariosApi.crear(match { it.rol == Rol.DUENO }, "admin@lacasajojo.cl") }
        assertTrue(repo.usuarios.any { it.email == dueno.email })
    }

    @Test
    fun adminBloqueaYActivaUsuarios() = runTest {
        ingresarComo(admin)
        repo.refrescar()
        coEvery { usuariosApi.cambiarEstado(any(), any(), any()) } returns Response.success(dueno.copy(activo = false))

        repo.cambiarEstadoUsuario(dueno.email)

        coVerify { usuariosApi.cambiarEstado(dueno.email, false, admin.email) }
        assertFalse(repo.usuarios.first { it.email == dueno.email }.activo)
    }

    @Test
    fun clienteNoPuedeCambiarEstadoDeUsuarios() = runTest {
        ingresarComo(cliente)
        repo.cambiarEstadoUsuario("dueno@lacasajojo.cl")
        coVerify(exactly = 0) { usuariosApi.cambiarEstado(any(), any(), any()) }
    }

    @Test
    fun cambiarEstadoDeUsuarioInexistenteNoLlamaAlServidor() = runTest {
        ingresarComo(admin)
        repo.cambiarEstadoUsuario("nadie@correo.cl")
        coVerify(exactly = 0) { usuariosApi.cambiarEstado(any(), any(), any()) }
    }

    // ---------------------------------------------------------------- platos (CRUD)
    @Test
    fun crearPlatoAgregaElPlatoConElIdDelServidor() = runTest {
        coEvery { menuApi.crearPlato(any()) } returns Response.success(201, Plato(7, "Lasaña", 5000))
        assertTrue(repo.guardarPlato(Plato(0, "Lasaña", 5000)))
        assertEquals(7, repo.platos.single().id)
        coVerify { dataStore.savePlatos(any()) }
    }

    @Test
    fun fotoLocalSeSubeAntesDeCrearElPlato() = runTest {
        val temporal = File.createTempFile("plato_demo_", ".jpg")
        try {
            temporal.writeBytes(byteArrayOf(1, 2, 3)) // El mock no necesita un JPG real
            val ruta = "/api/fotos/00000000-0000-0000-0000-000000000001.jpg"
            coEvery { menuApi.subirFoto(any()) } returns Response.success(201, FotoSubidaResponse(ruta))
            coEvery { menuApi.crearPlato(any()) } returns Response.success(201, Plato(8, "Lasaña", 5000, foto = ruta))
            assertTrue(repo.guardarPlato(Plato(0, "Lasaña", 5000, foto = temporal.absolutePath)))
            coVerify(exactly = 1) { menuApi.subirFoto(any()) }
            coVerify { menuApi.crearPlato(match { it.foto == ruta }) }
        } finally {
            temporal.delete()
        }
    }

    @Test
    fun fotoPerdidaNoSeGuardaComoRutaDeOtroDispositivo() = runTest {
        assertFalse(repo.guardarPlato(Plato(0, "Lasaña", 5000, foto = "/ruta/inexistente.jpg")))
        assertNotNull(repo.mensajeRed)
        coVerify(exactly = 0) { menuApi.crearPlato(any()) }
    }

    @Test
    fun fotoYaSubidaNoSeSubeDosVeces() = runTest {
        val ruta = "/api/fotos/00000000-0000-0000-0000-000000000001.jpg"
        coEvery { menuApi.crearPlato(any()) } returns Response.success(201, Plato(8, "Lasaña", 5000, foto = ruta))
        assertTrue(repo.guardarPlato(Plato(0, "Lasaña", 5000, foto = ruta)))
        coVerify(exactly = 0) { menuApi.subirFoto(any()) }
    }

    @Test
    fun editarPlatoReemplazaElExistente() = runTest {
        repo.refrescar()
        coEvery { menuApi.actualizarPlato(1, any()) } returns Response.success(pollo.copy(precio = 5200))
        assertTrue(repo.guardarPlato(pollo.copy(precio = 5200)))
        assertEquals(5200, repo.platos.single { it.id == 1 }.precio)
        assertEquals(1, repo.platos.size)
    }

    @Test
    fun guardarPlatoConErrorDelServidor() = runTest {
        coEvery { menuApi.crearPlato(any()) } returns Response.error(400, "".toResponseBody(null))
        assertFalse(repo.guardarPlato(Plato(0, "Lasaña", 5000)))
        assertNotNull(repo.mensajeRed)
        repo.limpiarMensajeRed()
        assertNull(repo.mensajeRed)
    }

    @Test
    fun guardarPlatoSinConexion() = runTest {
        coEvery { menuApi.crearPlato(any()) } throws IOException()
        assertFalse(repo.guardarPlato(Plato(0, "Lasaña", 5000)))
        assertEquals(MSG_SIN_CONEXION, repo.mensajeRed)
    }

    @Test
    fun guardarPlatoConCuerpoVacio() = runTest {
        coEvery { menuApi.crearPlato(any()) } returns Response.success<Plato>(null)
        assertFalse(repo.guardarPlato(Plato(0, "Lasaña", 5000)))
    }

    @Test
    fun eliminarPlato() = runTest {
        repo.refrescar()
        coEvery { menuApi.eliminarPlato(1) } returns Response.success(Unit)
        assertTrue(repo.eliminarPlato(1))
        assertTrue(repo.platos.isEmpty())
    }

    @Test
    fun eliminarPlatoConErrorOSinConexion() = runTest {
        repo.refrescar()
        coEvery { menuApi.eliminarPlato(1) } returns Response.error(404, "".toResponseBody(null))
        assertFalse(repo.eliminarPlato(1))
        coEvery { menuApi.eliminarPlato(1) } throws IOException()
        assertFalse(repo.eliminarPlato(1))
        assertEquals(1, repo.platos.size)
    }

    @Test
    fun cambiarDisponibilidadDelPlato() = runTest {
        repo.refrescar()
        coEvery { menuApi.actualizarPlato(1, any()) } returns Response.success(pollo.copy(disponible = false))
        assertTrue(repo.cambiarDisponiblePlato(1))
        assertFalse(repo.platos.single().disponible)
        assertFalse(repo.cambiarDisponiblePlato(99))
    }

    // ---------------------------------------------------------------- agregados y ensaladas
    @Test
    fun agregarYEliminarExtra() = runTest {
        coEvery { menuApi.crearExtra(any()) } returns Response.success(201, Extra(5, "Papas", TipoExtra.AGREGADO))
        assertTrue(repo.agregarExtra(" Papas ", TipoExtra.AGREGADO))
        assertEquals("Papas", repo.extras.single().nombre)

        coEvery { menuApi.eliminarExtra(5) } returns Response.success(Unit)
        assertTrue(repo.eliminarExtra(5))
        assertTrue(repo.extras.isEmpty())
    }

    @Test
    fun agregarExtraConError() = runTest {
        coEvery { menuApi.crearExtra(any()) } returns Response.error(400, "".toResponseBody(null))
        assertFalse(repo.agregarExtra("Papas", TipoExtra.AGREGADO))
        coEvery { menuApi.crearExtra(any()) } throws IOException()
        assertFalse(repo.agregarExtra("Papas", TipoExtra.AGREGADO))
        coEvery { menuApi.eliminarExtra(any()) } returns Response.error(404, "".toResponseBody(null))
        assertFalse(repo.eliminarExtra(1))
    }

    @Test
    fun cambiarDisponibilidadDelExtra() = runTest {
        repo.refrescar()
        coEvery { menuApi.actualizarExtra(1, any()) } returns Response.success(arroz.copy(disponible = false))
        assertTrue(repo.cambiarDisponibleExtra(1))
        assertFalse(repo.extras.single().disponible)
        assertFalse(repo.cambiarDisponibleExtra(99))
        coEvery { menuApi.actualizarExtra(1, any()) } returns Response.error(500, "".toResponseBody(null))
        assertFalse(repo.cambiarDisponibleExtra(1))
    }

    // ---------------------------------------------------------------- carrito y pedidos
    private fun item(cantidad: Int = 1) = ItemPedido("Pollo Asado", 4800, listOf("Arroz normal"), "Mix de ensaladas", cantidad)

    @Test
    fun carritoAgregaCambiaCantidadYQuita() {
        repo.agregarAlCarrito(item())
        repo.cambiarCantidad(0, +2)
        assertEquals(3, repo.carrito[0].cantidad)
        repo.cambiarCantidad(0, -5) // no baja de 1
        assertEquals(3, repo.carrito[0].cantidad)
        repo.cambiarCantidad(7, 1) // índice inexistente
        repo.quitarDelCarrito(7)
        repo.quitarDelCarrito(0)
        assertTrue(repo.carrito.isEmpty())
    }

    @Test
    fun confirmarPedidoUsaElNumeroDelServidorYVaciaElCarrito() = runTest {
        ingresarComo(cliente)
        repo.agregarAlCarrito(item(2))
        coEvery { pedidosApi.crear(any()) } answers {
            Response.success(201, firstArg<Pedido>().copy(numero = 15))
        }

        val pedido = repo.confirmarPedido("  sin sal ")

        assertNotNull(pedido)
        assertEquals(15, pedido!!.numero)
        assertEquals("sin sal", pedido.observaciones)
        assertEquals(9600, pedido.total())
        assertTrue(repo.carrito.isEmpty())
        assertEquals(pedido, repo.pedidoPorNumero(15))
        assertEquals(listOf(pedido), repo.pedidosDelCliente())
    }

    @Test
    fun confirmarPedidoSinCarritoOSinSerClienteNoHaceNada() = runTest {
        assertNull(repo.confirmarPedido("")) // sin sesión
        ingresarComo(cliente)
        assertNull(repo.confirmarPedido("")) // carrito vacío
        repo.logout()
        ingresarComo(dueno)
        repo.agregarAlCarrito(item())
        assertNull(repo.confirmarPedido("")) // el dueño no pide
        coVerify(exactly = 0) { pedidosApi.crear(any()) }
    }

    @Test
    fun confirmarPedidoConFallosConservaElCarrito() = runTest {
        ingresarComo(cliente)
        repo.agregarAlCarrito(item())
        coEvery { pedidosApi.crear(any()) } throws IOException()
        assertNull(repo.confirmarPedido(""))
        coEvery { pedidosApi.crear(any()) } returns Response.error(400, "".toResponseBody(null))
        assertNull(repo.confirmarPedido(""))
        coEvery { pedidosApi.crear(any()) } returns Response.success<Pedido>(null)
        assertNull(repo.confirmarPedido(""))
        assertEquals(1, repo.carrito.size)
    }

    @Test
    fun pedidosDelClienteSinSesionEsListaVacia() {
        assertTrue(repo.pedidosDelCliente().isEmpty())
        assertNull(repo.pedidoPorNumero(1))
    }

    @Test
    fun duenoCambiaElEstadoDeUnPedido() = runTest {
        val p = Pedido(3, "ana@correo.cl", "Ana", "hoy", listOf(item()), "", EstadoPedido.RECIBIDO)
        coEvery { pedidosApi.listar(null) } returns listOf(p)
        ingresarComo(dueno)
        repo.refrescar()
        coEvery { pedidosApi.cambiarEstado(3, EstadoPedido.EN_PREPARACION) } returns
            Response.success(p.copy(estado = EstadoPedido.EN_PREPARACION))

        assertTrue(repo.cambiarEstadoPedido(3, EstadoPedido.EN_PREPARACION))
        assertEquals(EstadoPedido.EN_PREPARACION, repo.pedidoPorNumero(3)?.estado)
    }

    @Test
    fun cambiarEstadoDePedidoSoloElDuenoYConRespuestaValida() = runTest {
        ingresarComo(cliente)
        assertFalse(repo.cambiarEstadoPedido(3, EstadoPedido.LISTO))
        repo.logout()
        ingresarComo(dueno)
        coEvery { pedidosApi.cambiarEstado(any(), any()) } returns Response.error(404, "".toResponseBody(null))
        assertFalse(repo.cambiarEstadoPedido(3, EstadoPedido.LISTO))
        coEvery { pedidosApi.cambiarEstado(any(), any()) } throws IOException()
        assertFalse(repo.cambiarEstadoPedido(3, EstadoPedido.LISTO))
    }
}
