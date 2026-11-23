package com.app.lacasajojo.viewmodel

import com.app.lacasajojo.Fixture
import com.app.lacasajojo.MainDispatcherRule
import com.app.lacasajojo.model.EstadoPedido
import com.app.lacasajojo.model.Extra
import com.app.lacasajojo.model.Pedido
import com.app.lacasajojo.model.TipoExtra
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class DuenoViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val f = Fixture()
    private val vm by lazy { DuenoViewModel(f.repo) }

    @Test
    fun agregaUnaEnsaladaYLimpiaElFormulario() = runTest {
        coEvery { f.menuApi.crearExtra(any()) } returns Response.success(201, Extra(9, "Lechuga", TipoExtra.ENSALADA))
        vm.onTipoExtraChange(1)
        vm.onNombreExtraChange("Lechuga")
        vm.onAgregarExtra()
        coVerify { f.menuApi.crearExtra(match { it.tipo == TipoExtra.ENSALADA && it.nombre == "Lechuga" }) }
        assertEquals("", vm.formExtra.value.nombre)
        assertEquals(1, vm.formExtra.value.tipo)
    }

    @Test
    fun nombreInvalidoNoSeEnvia() = runTest {
        vm.onNombreExtraChange("ab")
        vm.onAgregarExtra()
        assertNotNull(vm.formExtra.value.errorNombre)
        coVerify(exactly = 0) { f.menuApi.crearExtra(any()) }
    }

    @Test
    fun siElServidorFallaElFormularioConservaElTexto() = runTest {
        coEvery { f.menuApi.crearExtra(any()) } throws java.io.IOException()
        vm.onNombreExtraChange("Papas fritas")
        vm.onAgregarExtra()
        assertEquals("Papas fritas", vm.formExtra.value.nombre)
    }

    @Test
    fun accionesDelMenuLlamanAlServidor() = runTest {
        f.repo.refrescar()
        coEvery { f.menuApi.actualizarPlato(any(), any()) } returns Response.success(f.pollo.copy(disponible = false))
        coEvery { f.menuApi.eliminarPlato(any()) } returns Response.success(Unit)
        coEvery { f.menuApi.actualizarExtra(any(), any()) } returns Response.success(f.arroz.copy(disponible = false))
        coEvery { f.menuApi.eliminarExtra(any()) } returns Response.success(Unit)

        vm.onCambiarDisponiblePlato(1)
        vm.onCambiarDisponibleExtra(1)
        vm.onEliminarExtra(1)
        vm.onEliminarPlato(1)

        coVerify { f.menuApi.actualizarPlato(1, match { !it.disponible }) }
        coVerify { f.menuApi.actualizarExtra(1, match { !it.disponible }) }
        coVerify { f.menuApi.eliminarExtra(1) }
        coVerify { f.menuApi.eliminarPlato(1) }
    }

    @Test
    fun cambiaElEstadoDeUnPedido() = runTest {
        f.ingresarComo(f.dueno)
        val p = Pedido(3, "ana@correo.cl", "Ana", "hoy", emptyList(), "", EstadoPedido.RECIBIDO)
        coEvery { f.pedidosApi.cambiarEstado(3, EstadoPedido.LISTO) } returns Response.success(p.copy(estado = EstadoPedido.LISTO))
        vm.onCambiarEstadoPedido(3, EstadoPedido.LISTO)
        coVerify { f.pedidosApi.cambiarEstado(3, EstadoPedido.LISTO) }
    }

    @Test
    fun salirCierraLaSesion() = runTest {
        f.ingresarComo(f.dueno)
        vm.onSalir()
        assertEquals(null, f.repo.usuarioActual)
    }
}
