package com.app.lacasajojo.viewmodel

import com.app.lacasajojo.Fixture
import com.app.lacasajojo.MainDispatcherRule
import com.app.lacasajojo.repository.MSG_CORREO_DUPLICADO
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class AdminViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val f = Fixture()
    private val vm by lazy { AdminViewModel(f.repo) }

    private fun llenar() {
        vm.onNombreChange("Pedro Soto")
        vm.onEmailChange("pedro@correo.cl")
        vm.onPasswordChange("Clave123")
    }

    @Test
    fun validaLosCamposAlEscribir() {
        vm.onNombreChange("1")
        vm.onEmailChange("x")
        vm.onPasswordChange("a")
        val e = vm.estado.value
        assertNotNull(e.errorNombre)
        assertNotNull(e.errorEmail)
        assertNotNull(e.errorPassword)
    }

    @Test
    fun formularioInvalidoNoLlamaAlServidor() = runTest {
        vm.onEnviar()
        coVerify(exactly = 0) { f.usuariosApi.crear(any(), any()) }
        assertNotNull(vm.estado.value.errorNombre)
    }

    @Test
    fun creaCuentaDeDueno() = runTest {
        f.ingresarComo(f.admin)
        coEvery { f.usuariosApi.crear(any(), any()) } returns Response.success(201, f.dueno)
        llenar()
        vm.onEnviar()
        assertEquals("Cuenta de dueño creada", vm.estado.value.mensajeExito)
        assertEquals("", vm.estado.value.nombre)
    }

    @Test
    fun correoRepetidoMuestraElError() = runTest {
        f.ingresarComo(f.admin)
        coEvery { f.usuariosApi.crear(any(), any()) } returns Response.error(409, "".toResponseBody(null))
        llenar()
        vm.onEnviar()
        assertEquals(MSG_CORREO_DUPLICADO, vm.estado.value.errorEmail)
        assertNull(vm.estado.value.mensajeExito)
    }

    @Test
    fun bloqueaUnUsuario() = runTest {
        f.ingresarComo(f.admin)
        coEvery { f.usuariosApi.cambiarEstado(any(), any(), any()) } returns Response.success(f.dueno.copy(activo = false))
        vm.onCambiarEstadoUsuario(f.dueno.email)
        coVerify { f.usuariosApi.cambiarEstado(f.dueno.email, false, f.admin.email) }
        assertEquals(f.admin.email, vm.emailActual)
    }

    @Test
    fun salirCierraLaSesion() = runTest {
        f.ingresarComo(f.admin)
        vm.onSalir()
        assertNull(vm.emailActual)
    }
}
