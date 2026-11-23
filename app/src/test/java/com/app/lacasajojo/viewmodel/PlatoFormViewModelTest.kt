package com.app.lacasajojo.viewmodel

import com.app.lacasajojo.Fixture
import com.app.lacasajojo.MainDispatcherRule
import com.app.lacasajojo.model.Plato
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class PlatoFormViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val f = Fixture()

    @Test
    fun platoNuevoEmpiezaVacio() {
        val vm = PlatoFormViewModel(f.repo, 0)
        assertTrue(vm.estado.value.esNuevo)
        assertEquals("", vm.estado.value.nombre)
    }

    @Test
    fun platoExistenteCargaSusDatos() = runTest {
        f.repo.refrescar()
        val vm = PlatoFormViewModel(f.repo, 1)
        assertFalse(vm.estado.value.esNuevo)
        assertEquals("Pollo Asado", vm.estado.value.nombre)
        assertEquals("4800", vm.estado.value.precio)
    }

    @Test
    fun validaNombreYPrecio() = runTest {
        val vm = PlatoFormViewModel(f.repo, 0)
        vm.onEnviar()
        assertNotNull(vm.estado.value.errorNombre)
        assertNotNull(vm.estado.value.errorPrecio)
        vm.onNombreChange("Lasaña")
        vm.onPrecioChange("abc")
        assertNull(vm.estado.value.errorNombre)
        assertNotNull(vm.estado.value.errorPrecio)
        coVerify(exactly = 0) { f.menuApi.crearPlato(any()) }
    }

    @Test
    fun guardaUnPlatoNuevo() = runTest {
        coEvery { f.menuApi.crearPlato(any()) } returns Response.success(201, Plato(8, "Lasaña", 5000))
        val vm = PlatoFormViewModel(f.repo, 0)
        vm.onNombreChange("Lasaña")
        vm.onPrecioChange("5000")
        vm.onEnviar()
        assertTrue(vm.estado.value.guardado)
        coVerify { f.menuApi.crearPlato(match { it.id == 0 && it.nombre == "Lasaña" && it.precio == 5000 }) }
    }

    @Test
    fun editaUnPlatoExistente() = runTest {
        f.repo.refrescar()
        coEvery { f.menuApi.actualizarPlato(1, any()) } returns Response.success(f.pollo.copy(precio = 5100))
        val vm = PlatoFormViewModel(f.repo, 1)
        vm.onPrecioChange("5100")
        vm.onEnviar()
        assertTrue(vm.estado.value.guardado)
    }

    @Test
    fun sinConexionElFormularioNoSeCierra() = runTest {
        coEvery { f.menuApi.crearPlato(any()) } throws java.io.IOException()
        val vm = PlatoFormViewModel(f.repo, 0)
        vm.onNombreChange("Lasaña")
        vm.onPrecioChange("5000")
        vm.onEnviar()
        assertFalse(vm.estado.value.guardado)
    }

    @Test
    fun manejaLosResultadosDeLaCamara() {
        val vm = PlatoFormViewModel(f.repo, 0)
        vm.onFotoTomada(null)
        assertNotNull(vm.estado.value.mensajeCamara)
        vm.onFotoTomada("/data/foto.jpg")
        assertEquals("/data/foto.jpg", vm.estado.value.foto)
        assertNull(vm.estado.value.mensajeCamara)
        vm.onPermisoCamaraDenegado()
        assertTrue(vm.estado.value.permisoDenegado)
        vm.onCamaraNoDisponible()
        assertNotNull(vm.estado.value.mensajeCamara)
    }
}
