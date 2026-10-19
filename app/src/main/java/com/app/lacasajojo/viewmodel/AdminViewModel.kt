package com.app.lacasajojo.viewmodel

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lacasajojo.model.Usuario
import com.app.lacasajojo.repository.AppRepository
import com.app.lacasajojo.utils.validarEmail
import com.app.lacasajojo.utils.validarNombre
import com.app.lacasajojo.utils.validarPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class AdminUiState(
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val errorNombre: String? = null,
    val errorEmail: String? = null,
    val errorPassword: String? = null,
    val mensajeExito: String? = null
)

// Administrador: crea cuentas de dueño y bloquea o activa usuarios
class AdminViewModel(private val repository: AppRepository) : ViewModel() {

    private val _estado = MutableStateFlow(AdminUiState())
    val estado: StateFlow<AdminUiState> = _estado.asStateFlow()

    val usuarios: StateFlow<List<Usuario>> = snapshotFlow { repository.usuarios.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.usuarios.toList())

    val emailActual: String? get() = repository.usuarioActual?.email

    fun onNombreChange(valor: String) {
        _estado.update { it.copy(nombre = valor, errorNombre = validarNombre(valor), mensajeExito = null) }
    }

    fun onEmailChange(valor: String) {
        _estado.update { it.copy(email = valor, errorEmail = validarEmail(valor), mensajeExito = null) }
    }

    fun onPasswordChange(valor: String) {
        _estado.update { it.copy(password = valor, errorPassword = validarPassword(valor), mensajeExito = null) }
    }

    fun onEnviar() {
        val e = _estado.value
        val errorNombre = validarNombre(e.nombre)
        val errorEmail = validarEmail(e.email)
        val errorPassword = validarPassword(e.password)
        if (errorNombre != null || errorEmail != null || errorPassword != null) {
            _estado.update {
                it.copy(errorNombre = errorNombre, errorEmail = errorEmail, errorPassword = errorPassword)
            }
            return
        }
        if (!repository.crearCuentaDueno(e.nombre, e.email, e.password)) {
            _estado.update { it.copy(errorEmail = "Ya existe una cuenta con ese correo") }
        } else {
            _estado.value = AdminUiState(mensajeExito = "Cuenta de dueño creada")
        }
    }

    fun onCambiarEstadoUsuario(email: String) = repository.cambiarEstadoUsuario(email)

    fun onSalir() = repository.logout()
}