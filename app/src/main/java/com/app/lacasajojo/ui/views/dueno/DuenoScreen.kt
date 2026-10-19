package com.app.lacasajojo.ui.views.dueno

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.app.lacasajojo.model.EstadoPedido
import com.app.lacasajojo.model.Extra
import com.app.lacasajojo.model.Plato
import com.app.lacasajojo.model.TipoExtra
import com.app.lacasajojo.navigation.Rutas
import com.app.lacasajojo.ui.components.BarraSuperior
import com.app.lacasajojo.ui.components.BotonJoJo
import com.app.lacasajojo.ui.components.BotonTexto
import com.app.lacasajojo.ui.components.CampoTexto
import com.app.lacasajojo.ui.components.FotoPlato
import com.app.lacasajojo.ui.components.Pestanas
import com.app.lacasajojo.ui.components.TarjetaJoJo
import com.app.lacasajojo.utils.MAX_PLATO
import com.app.lacasajojo.utils.clp
import com.app.lacasajojo.utils.formatoBoleta
import com.app.lacasajojo.viewmodel.AppViewModelProvider
import com.app.lacasajojo.viewmodel.DuenoViewModel

// Dueño: decide qué se vende (CRUD del menú) y gestiona los pedidos
@Composable
fun DuenoScreen(
    navController: NavHostController,
    viewModel: DuenoViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    var pestana by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        topBar = { BarraSuperior("Panel del dueño", "Salir") { viewModel.onSalir() } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Pestanas(listOf("Platos", "Extras", "Pedidos"), pestana) { pestana = it }
            when (pestana) {
                0 -> PlatosTab(navController, viewModel)
                1 -> ExtrasTab(viewModel)
                else -> PedidosTab(navController, viewModel)
            }
        }
    }
}

// ---------------------------------------------------------------------
// Pestaña Platos
// ---------------------------------------------------------------------
@Composable
private fun PlatosTab(navController: NavHostController, viewModel: DuenoViewModel) {
    val platos by viewModel.platos.collectAsState()
    var porEliminar by remember { mutableStateOf<Plato?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            BotonJoJo("Agregar plato", { navController.navigate(Rutas.platoForm(0)) }, Modifier.fillMaxWidth())
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(plato.nombre, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Text(plato.precio.clp(), fontWeight = FontWeight.Bold)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = plato.disponible,
                        onCheckedChange = { viewModel.onCambiarDisponiblePlato(plato.id) }
                    )
                    Text("  Disponible hoy")
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { navController.navigate(Rutas.platoForm(plato.id)) }) {
                        Text("Editar", color = MaterialTheme.colorScheme.onBackground)
                    }
                    BotonTexto("Eliminar", peligro = true) { porEliminar = plato }
                }
            }
        }
    }

    porEliminar?.let { plato ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar plato") },
            text = { Text("¿Seguro que quieres eliminar \"${plato.nombre}\"?") },
            confirmButton = {
                BotonTexto("Eliminar", peligro = true) {
                    viewModel.onEliminarPlato(plato.id)
                    porEliminar = null
                }
            },
            dismissButton = { BotonTexto("Cancelar") { porEliminar = null } }
        )
    }
}

// ---------------------------------------------------------------------
// Pestaña Extras (agregados y ensaladas)
// ---------------------------------------------------------------------
@Composable
private fun ExtrasTab(viewModel: DuenoViewModel) {
    val extras by viewModel.extras.collectAsState()
    val form by viewModel.formExtra.collectAsState()
    var porEliminar by remember { mutableStateOf<Extra?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column {
                Text(
                    "Agregar agregado o ensalada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                CampoTexto(
                    valor = form.nombre,
                    onCambio = viewModel::onNombreExtraChange,
                    etiqueta = "Nombre (ej.: Puré natural)",
                    icono = Icons.Filled.Restaurant,
                    error = form.errorNombre,
                    maxLength = MAX_PLATO
                )
                Pestanas(listOf("Agregado", "Ensalada"), form.tipo) { viewModel.onTipoExtraChange(it) }
                Spacer(Modifier.height(8.dp))
                BotonJoJo("Agregar", viewModel::onAgregarExtra, Modifier.fillMaxWidth())
            }
        }
        items(extras, key = { it.id }) { extra ->
            TarjetaJoJo(modifier = Modifier.animateItem()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(extra.nombre, fontWeight = FontWeight.Bold)
                        Text(if (extra.tipo == TipoExtra.AGREGADO) "Agregado" else "Ensalada")
                    }
                    Switch(
                        checked = extra.disponible,
                        onCheckedChange = { viewModel.onCambiarDisponibleExtra(extra.id) }
                    )
                }
                Text(if (extra.disponible) "Disponible hoy" else "No disponible")
                BotonTexto("Eliminar", peligro = true) { porEliminar = extra }
            }
        }
    }

    porEliminar?.let { extra ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("Eliminar") },
            text = { Text("¿Seguro que quieres eliminar \"${extra.nombre}\"?") },
            confirmButton = {
                BotonTexto("Eliminar", peligro = true) {
                    viewModel.onEliminarExtra(extra.id)
                    porEliminar = null
                }
            },
            dismissButton = { BotonTexto("Cancelar") { porEliminar = null } }
        )
    }
}

// ---------------------------------------------------------------------
// Pestaña Pedidos
// ---------------------------------------------------------------------
@Composable
private fun PedidosTab(navController: NavHostController, viewModel: DuenoViewModel) {
    val lista by viewModel.pedidos.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (lista.isEmpty()) {
            item { Text("Aún no hay pedidos.") }
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
                Text("${pedido.nombreCliente} · ${pedido.fecha}")
                pedido.items.forEach { item ->
                    Text("${item.cantidad} x ${item.plato}")
                }
                if (pedido.observaciones.isNotEmpty()) {
                    Text("Obs.: ${pedido.observaciones}")
                }
                Text("Estado: ${pedido.estado.texto}", fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val siguiente = pedido.estado.siguiente()
                    if (siguiente != null) {
                        BotonJoJo("Pasar a: ${siguiente.texto}", { viewModel.onCambiarEstadoPedido(pedido.numero, siguiente) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { navController.navigate(Rutas.boleta(pedido.numero)) }) {
                        Text("Ver boleta", color = MaterialTheme.colorScheme.onBackground)
                    }
                    if (pedido.estado == EstadoPedido.RECIBIDO || pedido.estado == EstadoPedido.EN_PREPARACION) {
                        BotonTexto("Cancelar pedido", peligro = true) {
                            viewModel.onCambiarEstadoPedido(pedido.numero, EstadoPedido.CANCELADO)
                        }
                    }
                }
            }
        }
    }
}
