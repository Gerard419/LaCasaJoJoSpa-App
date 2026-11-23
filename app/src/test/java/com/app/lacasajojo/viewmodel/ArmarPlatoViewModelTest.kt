package com.app.lacasajojo.viewmodel

import com.app.lacasajojo.Fixture
import com.app.lacasajojo.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ArmarPlatoViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val f = Fixture()

    private suspend fun crearVm(): ArmarPlatoViewModel {
        f.repo.refrescar()
        return ArmarPlatoViewModel(f.repo, 1)
    }

    @Test
    fun platoInexistenteNoSeAgregaAlCarrito() = runTest {
        f.repo.refrescar()
        val vm = ArmarPlatoViewModel(f.repo, 99)
        assertNull(vm.plato)
        vm.onEnviar()
        assertTrue(f.repo.carrito.isEmpty())
    }

    @Test
    fun maximoDosAgregados() = runTest {
        val vm = crearVm()
        vm.onAgregadoClick("Arroz normal")
        vm.onAgregadoClick("Puré (natural)")
        vm.onAgregadoClick("Fideos blancos")
        assertEquals(listOf("Arroz normal", "Puré (natural)"), vm.estado.value.agregados)
        assertNotNull(vm.estado.value.errorAgregados)
        vm.onAgregadoClick("Arroz normal") // desmarca
        assertEquals(listOf("Puré (natural)"), vm.estado.value.agregados)
        assertNull(vm.estado.value.errorAgregados)
    }

    @Test
    fun cantidadQuedaEntre1Y20() = runTest {
        val vm = crearVm()
        vm.onCantidadChange(-3)
        assertEquals(1, vm.estado.value.cantidad)
        vm.onCantidadChange(50)
        assertEquals(20, vm.estado.value.cantidad)
    }

    @Test
    fun faltaAgregadoYEnsaladaMuestraErrores() = runTest {
        val vm = crearVm()
        vm.onEnviar()
        assertNotNull(vm.estado.value.errorAgregados)
        assertNotNull(vm.estado.value.errorEnsalada)
        assertTrue(f.repo.carrito.isEmpty())
    }

    @Test
    fun plato_completo_se_agrega_al_carrito() = runTest {
        val vm = crearVm()
        vm.onAgregadoClick("Arroz normal")
        vm.onEnsaladaClick("Mix de ensaladas")
        vm.onCantidadChange(1)
        vm.onEnviar()
        assertTrue(vm.estado.value.agregadoAlCarrito)
        assertEquals(2, f.repo.carrito.single().cantidad)
        assertEquals(4800, f.repo.carrito.single().precioUnitario)
    }

    @Test
    fun losListadosDeExtrasPartenVacios() = runTest {
        val vm = crearVm()
        // Los StateFlow arrancan vacíos hasta que la pantalla los observa
        assertTrue(vm.agregadosDisponibles.value.isEmpty())
        assertTrue(vm.ensaladasDisponibles.value.isEmpty())
    }
}
