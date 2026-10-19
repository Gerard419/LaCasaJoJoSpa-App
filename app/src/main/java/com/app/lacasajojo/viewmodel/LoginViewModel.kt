package com.app.lacasajojo.viewmodel

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lacasajojo.model.Rol
import com.app.lacasajojo.repository.AppRepository
import com.app.lacasajojo.utils.validarEmail
import com.app.lacasajojo.utils.validarPasswordLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val errorEmail: String? = null,
    val errorPassword: String? = null,
    val errorGeneral: String? = null,
    val rolIngresado: Rol? = null // cuando no es null, la pantalla navega al inicio de ese rol
)

class LoginViewModel(private val repository: AppRepository) : ViewModel() {

    private val _estado = MutableStateFlow(LoginUiState())
    val estado: StateFlow<LoginUiState> = _estado.asStateFlow()

    // Los datos se cargan al abrir la app: el botón espera a que terminen
    val cargado: StateFlow<Boolean> = snapshotFlow { repository.cargado }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.cargado)

    fun onEmailChange(valor: String) {
        _estado.update { it.copy(email = valor, errorEmail = validarEmail(valor), errorGeneral = null) }
    }

    fun onPasswordChange(valor: String) {
        _estado.update {
            it.copy(password = valor, errorPassword = validarPasswordLogin(valor), errorGeneral = null)
        }
    }

    fun onEnviar() {
        val actual = _estado.value
        val errorEmail = validarEmail(actual.email)
        val errorPassword = validarPasswordLogin(actual.password)
        if (errorEmail != null || errorPassword != null) {
            _estado.update { it.copy(errorEmail = errorEmail, errorPassword = errorPassword) }
            return
        }
        val resultado = repository.login(actual.email, actual.password)
        val usuario = repository.usuarioActual
        if (resultado != null || usuario == null) {
            _estado.update { it.copy(errorGeneral = resultado ?: "No se pudo iniciar sesión") }
        } else {
            _estado.update { it.copy(errorGeneral = null, rolIngresado = usuario.rol) }
        }
    }

    fun onIngresoProcesado() {
        _estado.update { it.copy(rolIngresado = null) }
    }
}