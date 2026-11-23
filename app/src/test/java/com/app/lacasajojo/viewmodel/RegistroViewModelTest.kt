package com.app.lacasajojo.viewmodel

import com.app.lacasajojo.Fixture
import com.app.lacasajojo.MainDispatcherRule
import com.app.lacasajojo.repository.MSG_CORREO_DUPLICADO
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class RegistroViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val f = Fixture()
    private val vm by lazy { RegistroViewModel(f.repo) }

    private fun llenarFormularioValido() {
        vm.onNombreChange("Ana Pérez")
        vm.onEmailChange("ana@correo.cl")
        vm.onTelefonoChange("912345678")
        vm.onPasswordChange("Clave123")
        vm.onConfirmarChange("Clave123")
    }

    @Test
    fun formularioVacioMuestraTodosLosErrores() = runTest {
        vm.onEnviar()
        val e = vm.estado.value
        assertNotNull(e.errorNombre)
        assertNotNull(e.errorEmail)
        assertNotNull(e.errorTelefono)
        assertNotNull(e.errorPassword)
        assertNotNull(e.errorConfirmar)
        assertFalse(e.registrado)
        coVerify(exactly = 0) { f.usuariosApi.crear(any(), any()) }
    }

    @Test
    fun contrasenaYConfirmacionSeComparanEnAmbosSentidos() {
        vm.onPasswordChange("Clave123")
        vm.onConfirmarChange("Otra1234")
        assertEquals("Las contraseñas no coinciden", vm.estado.value.errorConfirmar)
        vm.onPasswordChange("Otra1234")
        assertNull(vm.estado.value.errorConfirmar)
    }

    @Test
    fun registroCorrecto() = runTest {
        coEvery { f.usuariosApi.crear(any(), any()) } returns Response.success(201, f.cliente)
        llenarFormularioValido()
        vm.onEnviar()
        assertTrue(vm.estado.value.registrado)
    }

    @Test
    fun correoRepetidoMuestraElError() = runTest {
        coEvery { f.usuariosApi.crear(any(), any()) } returns Response.error(409, "".toResponseBody(null))
        llenarFormularioValido()
        vm.onEnviar()
        assertEquals(MSG_CORREO_DUPLICADO, vm.estado.value.errorEmail)
        assertFalse(vm.estado.value.registrado)
    }
}