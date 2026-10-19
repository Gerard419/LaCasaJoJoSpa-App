package com.app.lacasajojo.viewmodel

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lacasajojo.model.Extra
import com.app.lacasajojo.model.ItemPedido
import com.app.lacasajojo.model.Plato
import com.app.lacasajojo.model.TipoExtra
import com.app.lacasajojo.repository.AppRepository
import com.app.lacasajojo.utils.validarAgregados
import com.app.lacasajojo.utils.validarEnsalada
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class ArmarPlatoUiState(
    val agregados: List<String> = emptyList(),
    val ensalada: String = "",
    val cantidad: Int = 1,
    val errorAgregados: String? = null,
    val errorEnsalada: String? = null,
    val agregadoAlCarrito: Boolean = false
)

// Armar el plato: 1 o 2 agregados + 1 ensalada (reglas RN-01 y RF del ERS)
class ArmarPlatoViewModel(
    private val repository: AppRepository,
    platoId: Int
) : ViewModel() {

    val plato: Plato? = repository.platos.find { it.id == platoId && it.disponible }

    val agregadosDisponibles: StateFlow<List<Extra>> = snapshotFlow {
        repository.extras.filter { it.tipo == TipoExtra.AGREGADO && it.disponible }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val ensaladasDisponibles: StateFlow<List<Extra>> = snapshotFlow {
        repository.extras.filter { it.tipo == TipoExtra.ENSALADA && it.disponible }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _estado = MutableStateFlow(ArmarPlatoUiState())
    val estado: StateFlow<ArmarPlatoUiState> = _estado.asStateFlow()

    fun onAgregadoClick(nombre: String) {
        _estado.update { e ->
            when {
                nombre in e.agregados -> e.copy(agregados = e.agregados - nombre, errorAgregados = null)
                e.agregados.size >= 2 -> e.copy(errorAgregados = validarAgregados(e.agregados.size + 1))
                else -> e.copy(agregados = e.agregados + nombre, errorAgregados = null)
            }
        }
    }

    fun onEnsaladaClick(nombre: String) {
        _estado.update { it.copy(ensalada = nombre, errorEnsalada = null) }
    }

    fun onCantidadChange(delta: Int) {
        _estado.update { it.copy(cantidad = (it.cantidad + delta).coerceIn(1, 20)) }
    }

    fun onEnviar() {
        val e = _estado.value
        val platoElegido = plato ?: return
        val errorAgregados = validarAgregados(e.agregados.size)
        val errorEnsalada = validarEnsalada(e.ensalada)
        if (errorAgregados != null || errorEnsalada != null) {
            _estado.update { it.copy(errorAgregados = errorAgregados, errorEnsalada = errorEnsalada) }
            return
        }
        repository.agregarAlCarrito(
            ItemPedido(
                plato = platoElegido.nombre,
                precioUnitario = platoElegido.precio,
                agregados = e.agregados,
                ensalada = e.ensalada,
                cantidad = e.cantidad
            )
        )
        _estado.update { it.copy(agregadoAlCarrito = true) }
    }
}
