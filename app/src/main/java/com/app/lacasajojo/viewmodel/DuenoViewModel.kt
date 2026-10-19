package com.app.lacasajojo.viewmodel

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lacasajojo.model.EstadoPedido
import com.app.lacasajojo.model.Extra
import com.app.lacasajojo.model.Pedido
import com.app.lacasajojo.model.Plato
import com.app.lacasajojo.model.TipoExtra
import com.app.lacasajojo.repository.AppRepository
import com.app.lacasajojo.utils.validarNombrePlato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ExtraFormUiState(
    val nombre: String = "",
    val tipo: Int = 0, // 0 = agregado, 1 = ensalada
    val errorNombre: String? = null
)

// Dueño: decide qué se vende (CRUD del menú) y gestiona los pedidos
class DuenoViewModel(private val repository: AppRepository) : ViewModel() {

    val platos: StateFlow<List<Plato>> = snapshotFlow { repository.platos.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.platos.toList())

    val extras: StateFlow<List<Extra>> = snapshotFlow { repository.extras.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.extras.toList())

    val pedidos: StateFlow<List<Pedido>> =
        snapshotFlow { repository.pedidos.sortedByDescending { it.numero } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _formExtra = MutableStateFlow(ExtraFormUiState())
    val formExtra: StateFlow<ExtraFormUiState> = _formExtra.asStateFlow()

    // ---- Platos
    fun onCambiarDisponiblePlato(id: Int) = repository.cambiarDisponiblePlato(id)
    fun onEliminarPlato(id: Int) = repository.eliminarPlato(id)

    // ---- Extras (agregados y ensaladas)
    fun onNombreExtraChange(valor: String) {
        _formExtra.update { it.copy(nombre = valor, errorNombre = validarNombrePlato(valor)) }
    }

    fun onTipoExtraChange(tipo: Int) {
        _formExtra.update { it.copy(tipo = tipo) }
    }

    fun onAgregarExtra() {
        val e = _formExtra.value
        val error = validarNombrePlato(e.nombre)
        if (error != null) {
            _formExtra.update { it.copy(errorNombre = error) }
            return
        }
        repository.agregarExtra(e.nombre.trim(), if (e.tipo == 0) TipoExtra.AGREGADO else TipoExtra.ENSALADA)
        _formExtra.value = ExtraFormUiState(tipo = e.tipo)
    }

    fun onCambiarDisponibleExtra(id: Int) = repository.cambiarDisponibleExtra(id)
    fun onEliminarExtra(id: Int) = repository.eliminarExtra(id)

    // ---- Pedidos
    fun onCambiarEstadoPedido(numero: Int, nuevo: EstadoPedido) = repository.cambiarEstadoPedido(numero, nuevo)

    fun onSalir() = repository.logout()
}
