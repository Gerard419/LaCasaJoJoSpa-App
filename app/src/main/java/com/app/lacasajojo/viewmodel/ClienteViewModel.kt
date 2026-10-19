package com.app.lacasajojo.viewmodel

import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.lacasajojo.model.ItemPedido
import com.app.lacasajojo.model.Pedido
import com.app.lacasajojo.model.Plato
import com.app.lacasajojo.repository.AppRepository
import com.app.lacasajojo.utils.MAX_OBSERVACIONES
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

data class CarritoUiState(
    val observaciones: String = "",
    val pedidoConfirmado: Int? = null // n° de boleta; la pantalla vibra y navega a la boleta
)

// Cliente: menú del día, carrito y sus pedidos
class ClienteViewModel(private val repository: AppRepository) : ViewModel() {

    // Menú del día agrupado por precio: lista de (precio, platos)
    val menu: StateFlow<List<Pair<Int, List<Plato>>>> = snapshotFlow {
        repository.platos.filter { it.disponible }.groupBy { it.precio }.toSortedMap().toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val carrito: StateFlow<List<ItemPedido>> = snapshotFlow { repository.carrito.toList() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), repository.carrito.toList())

    val misPedidos: StateFlow<List<Pedido>> = snapshotFlow { repository.pedidosDelCliente() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _estado = MutableStateFlow(CarritoUiState())
    val estado: StateFlow<CarritoUiState> = _estado.asStateFlow()

    fun onObservacionesChange(valor: String) {
        if (valor.length <= MAX_OBSERVACIONES) _estado.update { it.copy(observaciones = valor) }
    }

    fun onCambiarCantidad(index: Int, delta: Int) = repository.cambiarCantidad(index, delta)

    fun onQuitar(index: Int) = repository.quitarDelCarrito(index)

    fun onConfirmar() {
        val pedido = repository.confirmarPedido(_estado.value.observaciones)
        if (pedido != null) {
            _estado.value = CarritoUiState(pedidoConfirmado = pedido.numero)
        }
    }

    fun onPedidoMostrado() {
        _estado.update { it.copy(pedidoConfirmado = null) }
    }

    fun onSalir() = repository.logout()
}
