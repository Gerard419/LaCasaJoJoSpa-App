package com.app.lacasajojo.ui.views.cliente

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import cl.dsy1105.lacasajojo.navigation.Rutas
import cl.dsy1105.lacasajojo.ui.components.BarraSuperior
import cl.dsy1105.lacasajojo.ui.components.BotonJoJo
import cl.dsy1105.lacasajojo.ui.components.BotonTexto
import cl.dsy1105.lacasajojo.ui.components.CampoTexto
import cl.dsy1105.lacasajojo.ui.components.FotoPlato
import cl.dsy1105.lacasajojo.ui.components.Pestanas
import cl.dsy1105.lacasajojo.ui.components.TarjetaJoJo
import cl.dsy1105.lacasajojo.utils.MAX_OBSERVACIONES
import cl.dsy1105.lacasajojo.utils.clp
import cl.dsy1105.lacasajojo.utils.formatoBoleta
import cl.dsy1105.lacasajojo.utils.vibrar
import cl.dsy1105.lacasajojo.viewmodel.AppViewModelProvider
import cl.dsy1105.lacasajojo.viewmodel.ClienteViewModel

// Cliente: ve el menú del día, arma su pedido y consulta sus boletas
@Composable
fun ClienteScreen(
    navController: NavHostController,
    viewModel: ClienteViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var pestana by rememberSaveable { mutableStateOf(0) }
    val carrito by viewModel.carrito.collectAsState()
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    // Pedido confirmado: vibra (recurso nativo) y abre la boleta
    LaunchedEffect(estado.pedidoConfirmado) {
        val numero = estado.pedidoConfirmado ?: return@LaunchedEffect
        vibrar(context, 150)
        navController.navigate(Rutas.boleta(numero))
        viewModel.onPedidoMostrado()
    }

    Scaffold(
        topBar = { BarraSuperior("La casa JoJo", "Salir") { viewModel.onSalir() } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val etiquetaCarrito = if (carrito.isEmpty()) "Carrito" else "Carrito (${carrito.size})"
            Pestanas(listOf("Menú", etiquetaCarrito, "Mis pedidos"), pestana) { pestana = it }
            when (pestana) {
                0 -> MenuTab(navController, viewModel)
                1 -> CarritoTab(viewModel)
                else -> MisPedidosTab(navController, viewModel)
            }
        }
    }
}

// ---------------------------------------------------------------------
// Pestaña Menú del día (agrupado por precio)
// ---------------------------------------------------------------------
@Composable
private fun MenuTab(navController: NavHostController, viewModel: ClienteViewModel) {
    val grupos by viewModel.menu.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                "Menú del día",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        item { Text("Todo plato incluye 1 o 2 agregados y una ensalada.") }
        if (grupos.isEmpty()) {
            item { Text("Hoy no hay platos disponibles.") }
        }
        for ((precio, platos) in grupos) {
            item(key = "precio_$precio") {
                Text(
                    precio.clp(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(platos, key = { it.id }) { plato ->
                TarjetaJoJo(modifier = Modifier.animateItem()) {
                    FotoPlato(
                        ruta = plato.foto,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(plato.nombre, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        BotonJoJo("Elegir", { navController.navigate(Rutas.armar(plato.id)) })
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// Pestaña Carrito
// ---------------------------------------------------------------------
@Composable
private fun CarritoTab(viewModel: ClienteViewModel) {
    val carrito by viewModel.carrito.collectAsState()
    val estado by viewModel.estado.collectAsState()
    val total = carrito.sumOf { it.subtotal() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (carrito.isEmpty()) {
            item { Text("Tu carrito está vacío. Elige un plato en la pestaña Menú.") }
        }
        itemsIndexed(carrito) { index, item ->
            TarjetaJoJo {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.plato, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(item.subtotal().clp(), fontWeight = FontWeight.Bold)
                }
                Text("Agregados: ${item.agregados.joinToString(", ")}")
                Text("Ensalada: ${item.ensalada}")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(onClick = { viewModel.onCambiarCantidad(index, -1) }) {
                        Text("-", color = MaterialTheme.colorScheme.onBackground)
                    }
                    Text("${item.cantidad}", fontWeight = FontWeight.Bold)
                    OutlinedButton(onClick = { viewModel.onCambiarCantidad(index, 1) }) {
                        Text("+", color = MaterialTheme.colorScheme.onBackground)
                    }
                    BotonTexto("Quitar", peligro = true) { viewModel.onQuitar(index) }
                }
            }
        }
        if (carrito.isNotEmpty()) {
            item {
                Column {
                    CampoTexto(
                        valor = estado.observaciones,
                        onCambio = viewModel::onObservacionesChange,
                        etiqueta = "Observaciones (ej.: sin cebolla)",
                        icono = Icons.Filled.Edit,
                        maxLength = MAX_OBSERVACIONES,
                        singleLine = false
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Total",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            total.clp(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    BotonJoJo("Confirmar pedido", viewModel::onConfirmar, Modifier.fillMaxWidth())
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// Pestaña Mis pedidos (historial de boletas)
// ---------------------------------------------------------------------
@Composable
private fun MisPedidosTab(navController: NavHostController, viewModel: ClienteViewModel) {
    val lista by viewModel.misPedidos.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (lista.isEmpty()) {
            item { Text("Aún no has hecho pedidos.") }
        }
        items(lista, key = { it.numero }) { pedido ->
            TarjetaJoJo(modifier = Modifier.animateItem()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Boleta N° ${pedido.numero.formatoBoleta()}", fontWeight = FontWeight.Bold)
                    Text(pedido.total().clp(), fontWeight = FontWeight.Bold)
                }
                Text(pedido.fecha)
                Text("Estado: ${pedido.estado.texto}")
                OutlinedButton(onClick = { navController.navigate(Rutas.boleta(pedido.numero)) }) {
                    Text("Ver boleta", color = MaterialTheme.colorScheme.onBackground)
                }
            }
        }
    }
}
