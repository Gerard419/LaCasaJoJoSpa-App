package com.app.lacasajojo

import com.app.lacasajojo.data.DataStoreManager
import com.app.lacasajojo.model.Extra
import com.app.lacasajojo.model.Plato
import com.app.lacasajojo.model.Rol
import com.app.lacasajojo.model.TipoExtra
import com.app.lacasajojo.model.Usuario
import com.app.lacasajojo.remote.BackendApi
import com.app.lacasajojo.remote.MenuApi
import com.app.lacasajojo.remote.PedidoApi
import com.app.lacasajojo.remote.UsuarioApi
import com.app.lacasajojo.repository.AppRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.Response

// Reemplaza el hilo principal de Android para poder probar los ViewModel en la JVM
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule : TestWatcher() {
    override fun starting(description: Description) = Dispatchers.setMain(UnconfinedTestDispatcher())
    override fun finished(description: Description) = Dispatchers.resetMain()
}

// Repositorio real con los microservicios simulados (MockK): sirve para probar los ViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class Fixture {
    val usuariosApi = mockk<UsuarioApi>()
    val menuApi = mockk<MenuApi>()
    val pedidosApi = mockk<PedidoApi>()
    val dataStore = mockk<DataStoreManager>(relaxed = true)

    val admin = Usuario("Administrador", "admin@lacasajojo.cl", "hash", Rol.ADMIN)
    val dueno = Usuario("Dueño", "dueno@lacasajojo.cl", "hash", Rol.DUENO)
    val cliente = Usuario("Ana", "ana@correo.cl", "hash", Rol.CLIENTE, telefono = "912345678")

    val pollo = Plato(1, "Pollo Asado", 4800)
    val arroz = Extra(1, "Arroz normal", TipoExtra.AGREGADO)
    val pure = Extra(2, "Puré (natural)", TipoExtra.AGREGADO)
    val fideos = Extra(3, "Fideos blancos", TipoExtra.AGREGADO)
    val mix = Extra(4, "Mix de ensaladas", TipoExtra.ENSALADA)

    val repo: AppRepository

    init {
        coEvery { menuApi.listarPlatos() } returns listOf(pollo)
        coEvery { menuApi.listarExtras() } returns listOf(arroz, pure, fideos, mix)
        coEvery { usuariosApi.listar(any()) } returns listOf(admin, dueno, cliente)
        coEvery { pedidosApi.listar(any()) } returns emptyList()
        repo = AppRepository(
            dataStore,
            BackendApi(usuariosApi, menuApi, pedidosApi),
            TestScope(UnconfinedTestDispatcher())
        )
    }

    suspend fun ingresarComo(usuario: Usuario) {
        coEvery { usuariosApi.login(any()) } returns Response.success(usuario)
        repo.login(usuario.email, "Clave123")
    }
}
