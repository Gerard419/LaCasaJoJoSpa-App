package com.app.lacasajojo.viewmodel

import com.app.lacasajojo.Fixture
import com.app.lacasajojo.MainDispatcherRule
import com.app.lacasajojo.model.Rol
import com.app.lacasajojo.repository.MSG_CREDENCIALES
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import retrofit2.Response

class LoginViewModelTest {
    @get:Rule val main = MainDispatcherRule()

    private val f = Fixture()
    private val vm by lazy { LoginViewModel(f.repo) }

    @Test
    fun camposVaciosMuestranErroresYNoLlamanAlServidor() = runTest {
        vm.onEnviar()
        assertNotNull(vm.estado.value.errorEmail)
        assertNotNull(vm.estado.value.errorPassword)
        coVerify(exactly = 0) { f.usuariosApi.login(any()) }
    }

    @Test
    fun validaEmailYPasswordMientrasSeEscribe() {
        vm.onEmailChange("malcorreo")
        assertNotNull(vm.estado.value.errorEmail)
        vm.onEmailChange("ana@correo.cl")
        assertNull(vm.estado.value.errorEmail)
        vm.onPasswordChange("")
        assertNotNull(vm.estado.value.errorPassword)
        vm.onPasswordChange("Clave123")
        assertNull(vm.estado.value.errorPassword)
    }

    @Test
    fun ingresoCorrectoEntregaElRol() = runTest {
        coEvery { f.usuariosApi.login(any()) } returns Response.success(f.dueno)
        vm.onEmailChange("dueno@lacasajojo.cl")
        vm.onPasswordChange("Dueno123")

        vm.onEnviar()

        assertEquals(Rol.DUENO, vm.estado.value.rolIngresado)
        assertNull(vm.estado.value.errorGeneral)
        vm.onIngresoProcesado()
        assertNull(vm.estado.value.rolIngresado)
    }

    @Test
    fun ingresoIncorrectoMuestraElError() = runTest {
        coEvery { f.usuariosApi.login(any()) } returns Response.error(401, "".toResponseBody(null))
        vm.onEmailChange("ana@correo.cl")
        vm.onPasswordChange("Mala1234")

        vm.onEnviar()

        assertEquals(MSG_CREDENCIALES, vm.estado.value.errorGeneral)
        assertNull(vm.estado.value.rolIngresado)
    }

    @Test
    fun alInicioElServidorSeConsideraDisponible() {
        assertTrue(vm.servidorDisponible.value)
    }
}
