package com.app.lacasajojo.viewmodel

import androidx.lifecycle.ViewModel
import com.app.lacasajojo.repository.AppRepository
import cl.dsy1105.lacasajojo.utils.validarConfirmacion
import cl.dsy1105.lacasajojo.utils.validarEmail
import cl.dsy1105.lacasajojo.utils.validarNombre
import cl.dsy1105.lacasajojo.utils.validarPassword
import cl.dsy1105.lacasajojo.utils.validarTelefono
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class RegistroUiState(
    val nombre: String = "",
    val email: String = "",
    val telefono: String = "",
    val password: String = "",
    val confirmar: String = "",
    val errorNombre: String? = null,
    val errorEmail: String? = null,
    val errorTelefono: String? = null,
    val errorPassword: String? = null,
    val errorConfirmar: String? = null,
    val registrado: Boolean = false
)

// RF-01: registro público, solo para clientes
class RegistroViewModel(private val repository: AppRepository) : ViewModel() {

    private val _estado = MutableStateFlow(RegistroUiState())
    val estado: StateFlow<RegistroUiState> = _estado.asStateFlow()

    fun onNombreChange(valor: String) {
        _estado.update { it.copy(nombre = valor, errorNombre = validarNombre(valor)) }
    }

    fun onEmailChange(valor: String) {
        _estado.update { it.copy(email = valor, errorEmail = validarEmail(valor)) }
    }

    fun onTelefonoChange(valor: String) {
        _estado.update { it.copy(telefono = valor, errorTelefono = validarTelefono(valor)) }
    }

    fun onPasswordChange(valor: String) {
        _estado.update {
            it.copy(
                password = valor,
                errorPassword = validarPassword(valor),
                // si ya escribió la confirmación, se vuelve a comparar
                errorConfirmar = if (it.confirmar.isEmpty()) it.errorConfirmar else validarConfirmacion(valor, it.confirmar)
            )
        }
    }

    fun onConfirmarChange(valor: String) {
        _estado.update { it.copy(confirmar = valor, errorConfirmar = validarConfirmacion(it.password, valor)) }
    }

    fun onEnviar() {
        val e = _estado.value
        val errorNombre = validarNombre(e.nombre)
        val errorEmail = validarEmail(e.email)
        val errorTelefono = validarTelefono(e.telefono)
        val errorPassword = validarPassword(e.password)
        val errorConfirmar = validarConfirmacion(e.password, e.confirmar)

        if (listOf(errorNombre, errorEmail, errorTelefono, errorPassword, errorConfirmar).any { it != null }) {
            _estado.update {
                it.copy(
                    errorNombre = errorNombre,
                    errorEmail = errorEmail,
                    errorTelefono = errorTelefono,
                    errorPassword = errorPassword,
                    errorConfirmar = errorConfirmar
                )
            }
            return
        }
        if (!repository.registrarCliente(e.nombre, e.email, e.telefono, e.password)) {
            _estado.update { it.copy(errorEmail = "Ya existe una cuenta con ese correo") }
        } else {
            _estado.update { it.copy(registrado = true) }
        }
    }
}