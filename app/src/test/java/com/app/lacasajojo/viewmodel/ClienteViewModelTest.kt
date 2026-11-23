package com.app.lacasajojo.viewmodel

import com.app.lacasajojo.Fixture
import com.app.lacasajojo.MainDispatcherRule
import com.app.lacasajojo.model.ItemPedido
import com.app.lacasajojo.model.Pedido
import com.app.lacasajojo.remote.ClimaActualDto
import com.app.lacasajojo.remote.ClimaApi
import com.app.lacasajojo.remote.ClimaResponse
import com.app.lacasajojo.repository.ClimaRepository
import com.app.lacasajojo.utils.MAX_OBSERVACIONES
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class ClienteViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val f = Fixture()
    private val item = ItemPedido("Pollo Asado", 4800, listOf("Arroz normal"), "Mix de ensaladas", 1)

    @Test
    fun cargaElClimaDeLaApiExterna() = runTest {
        val api = mockk<ClimaApi>()
        coEvery { api.clima(any(), any(), any()) } returns ClimaResponse(ClimaActualDto(18.4, 10.0, 0))
        val vm = ClienteViewModel(f.repo, ClimaRepository(api))
        assertEquals(18.4, vm.clima.value!!.temperatura, 0.001)
    }

    @Test
    fun sinApiDeClimaNoHayClima() {
        val vm = ClienteViewModel(f.repo)
        assertNull(vm.clima.value)
        vm.cargarClima()
        assertNull(vm.clima.value)
    }

    @Test
    fun observacionesRespetanElLargoMaximo() {
        val vm = ClienteViewModel(f.repo)
        vm.onObservacionesChange("sin sal")
        assertEquals("sin sal", vm.estado.value.observaciones)
        vm.onObservacionesChange("x".repeat(MAX_OBSERVACIONES + 1))
        assertEquals("sin sal", vm.estado.value.observaciones)
    }

    @Test
    fun manejaElCarrito() {
        val vm = ClienteViewModel(f.repo)
        f.repo.agregarAlCarrito(item)
        vm.onCambiarCantidad(0, 2)
        assertEquals(3, f.repo.carrito[0].cantidad)
        vm.onQuitar(0)
        assertEquals(0, f.repo.carrito.size)
    }

    @Test
    fun confirmarPedidoEntregaElNumeroDeBoleta() = runTest {
        f.ingresarComo(f.cliente)
        f.repo.agregarAlCarrito(item)
        coEvery { f.pedidosApi.crear(any()) } answers { Response.success(201, firstArg<Pedido>().copy(numero = 21)) }
        val vm = ClienteViewModel(f.repo)

        vm.onConfirmar()

        assertEquals(21, vm.estado.value.pedidoConfirmado)
        vm.onPedidoMostrado()
        assertNull(vm.estado.value.pedidoConfirmado)
    }

    @Test
    fun siNoSePudoConfirmarNoHayBoleta() = runTest {
        f.ingresarComo(f.cliente)
        f.repo.agregarAlCarrito(item)
        coEvery { f.pedidosApi.crear(any()) } throws java.io.IOException()
        val vm = ClienteViewModel(f.repo)
        vm.onConfirmar()
        assertNull(vm.estado.value.pedidoConfirmado)
        assertNotNull(f.repo.mensajeRed)
    }

    @Test
    fun salirCierraLaSesion() = runTest {
        f.ingresarComo(f.cliente)
        ClienteViewModel(f.repo).onSalir()
        assertNull(f.repo.usuarioActual)
    }
}
