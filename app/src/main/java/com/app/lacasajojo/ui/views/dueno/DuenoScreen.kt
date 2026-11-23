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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.app.lacasajojo.ui.components.TarjetaCatalogo
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
        topBar = { BarraSuperior("La casa JoJo · Dueño", "Salir") { viewModel.onSalir() } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Pestanas(listOf("Menú", "Pedidos"), pestana) { pestana = it }
            when (pestana) {
                0 -> MenuVisualTab(navController, viewModel)
                else -> PedidosTab(navController, viewModel)
            }
        }
    }
}

// Menú visual del video de entrega anterior: platos, agregados y ensaladas en carruseles.
// Las acciones usan el mismo ViewModel y los mismos microservicios que la versión EP4.
@Composable
private fun MenuVisualTab(navController: NavHostController, viewModel: DuenoViewModel) {
    val platos by viewModel.platos.collectAsState()
    val extras by viewModel.extras.collectAsState()
    val form by viewModel.formExtra.collectAsState()
    var platoPorEliminar by remember { mutableStateOf<Plato?>(null) }
    var extraPorEliminar by remember { mutableStateOf<Extra?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Plato de fondo", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Toca Editar para cambiar nombre, precio o imagen", style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                BotonJoJo("Agregar plato", { navController.navigate(Rutas.platoForm(0)) }, Modifier.fillMaxWidth())
            }
        }
        item {
            if (platos.isEmpty()) Text("Todavía no hay platos registrados.")
            else LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(platos, key = { it.id }) { plato ->
                    TarjetaCatalogo(nombre = plato.nombre, foto = plato.foto, precio = plato.precio.clp()) {
                        Switch(
                            checked = plato.disponible,
                            onCheckedChange = { viewModel.onCambiarDisponiblePlato(plato.id) },
                            modifier = Modifier.height(34.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                            OutlinedButton(
                                onClick = { navController.navigate(Rutas.platoForm(plato.id)) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp)
                            ) { Text("Editar", style = MaterialTheme.typography.labelSmall) }
                            TextButton(
                                onClick = { platoPorEliminar = plato },
                                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 0.dp),
                                modifier = Modifier.height(34.dp)
                            ) { Text("Eliminar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }
        }
        item { Text("Agregados", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            val agregados = extras.filter { it.tipo == TipoExtra.AGREGADO }
            if (agregados.isEmpty()) Text("No hay agregados.")
            else LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(agregados, key = { it.id }) { extra ->
                    TarjetaCatalogo(nombre = extra.nombre, foto = null) {
                        Switch(
                            checked = extra.disponible,
                            onCheckedChange = { viewModel.onCambiarDisponibleExtra(extra.id) },
                            modifier = Modifier.height(34.dp)
                        )
                        TextButton(
                            onClick = { extraPorEliminar = extra },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(30.dp)
                        ) { Text("Eliminar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
        item { Text("Ensaladas", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold) }
        item {
            val ensaladas = extras.filter { it.tipo == TipoExtra.ENSALADA }
            if (ensaladas.isEmpty()) Text("No hay ensaladas.")
            else LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(ensaladas, key = { it.id }) { extra ->
                    TarjetaCatalogo(nombre = extra.nombre, foto = null) {
                        Switch(
                            checked = extra.disponible,
                            onCheckedChange = { viewModel.onCambiarDisponibleExtra(extra.id) },
                            modifier = Modifier.height(34.dp)
                        )
                        TextButton(
                            onClick = { extraPorEliminar = extra },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(30.dp)
                        ) { Text("Eliminar", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error) }
                    }
                }
            }
        }
        item {
            TarjetaJoJo {
                Text("Añadir agregado o ensalada", fontWeight = FontWeight.Bold)
                CampoTexto(
                    valor = form.nombre,
                    onCambio = viewModel::onNombreExtraChange,
                    etiqueta = "Nombre (ej.: Puré natural)",
                    icono = Icons.Filled.Restaurant,
                    error = form.errorNombre,
                    maxLength = MAX_PLATO
                )
                Pestanas(listOf("Agregado", "Ensalada"), form.tipo) { viewModel.onTipoExtraChange(it) }
                BotonJoJo("Agregar", viewModel::onAgregarExtra, Modifier.fillMaxWidth())
            }
        }
    }

    platoPorEliminar?.let { plato ->
        AlertDialog(
            onDismissRequest = { platoPorEliminar = null },
            title = { Text("Eliminar plato") },
            text = { Text("¿Seguro que quieres eliminar \"${plato.nombre}\"?") },
            confirmButton = { BotonTexto("Eliminar", peligro = true) {
                viewModel.onEliminarPlato(plato.id)
                platoPorEliminar = null
            } },
            dismissButton = { BotonTexto("Cancelar") { platoPorEliminar = null } }
        )
    }
    extraPorEliminar?.let { extra ->
        AlertDialog(
            onDismissRequest = { extraPorEliminar = null },
            title = { Text("Eliminar agregado o ensalada") },
            text = { Text("¿Eliminar \"${extra.nombre}\"?") },
            confirmButton = { BotonTexto("Eliminar", peligro = true) {
                viewModel.onEliminarExtra(extra.id)
                extraPorEliminar = null
            } },
            dismissButton = { BotonTexto("Cancelar") { extraPorEliminar = null } }
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
