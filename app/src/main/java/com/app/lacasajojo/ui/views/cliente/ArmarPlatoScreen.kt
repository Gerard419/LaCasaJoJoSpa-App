package com.app.lacasajojo.ui.views.cliente

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.app.lacasajojo.ui.components.BarraSuperior
import com.app.lacasajojo.ui.components.BotonJoJo
import com.app.lacasajojo.ui.components.FotoPlato
import com.app.lacasajojo.ui.components.FotoCatalogo
import com.app.lacasajojo.ui.components.TarjetaCatalogo
import com.app.lacasajojo.ui.components.MensajeError
import com.app.lacasajojo.utils.clp
import com.app.lacasajojo.viewmodel.AppViewModelProvider
import com.app.lacasajojo.viewmodel.ArmarPlatoViewModel

// Armar el plato: 1 o 2 agregados + 1 ensalada (reglas RN-01 y RF del ERS)
@Composable
fun ArmarPlatoScreen(navController: NavHostController, platoId: Int) {
    val viewModel: ArmarPlatoViewModel = viewModel(
        key = "armar_$platoId",
        factory = AppViewModelProvider.armarPlato(platoId)
    )
    val estado by viewModel.estado.collectAsState()
    val agregados by viewModel.agregadosDisponibles.collectAsState()
    val ensaladas by viewModel.ensaladasDisponibles.collectAsState()
    val plato = viewModel.plato

    LaunchedEffect(estado.agregadoAlCarrito) {
        if (estado.agregadoAlCarrito) navController.popBackStack()
    }

    Scaffold(
        topBar = { BarraSuperior("Arma tu plato", onVolver = { navController.popBackStack() }) }
    ) { padding ->
        if (plato == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
            ) {
                Text("Este plato ya no está disponible.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                FotoCatalogo(
                    nombre = plato.nombre,
                    ruta = plato.foto,
                    modifier = Modifier.fillMaxWidth().height(145.dp)
                )
                Text(
                    plato.nombre,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(plato.precio.clp(), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(12.dp))

                Text(
                    "Elige 1 o 2 agregados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (agregados.isEmpty()) Text("No hay agregados disponibles hoy.")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(agregados.size) { indice ->
                        val extra = agregados[indice]
                        val elegido = extra.nombre in estado.agregados
                        TarjetaCatalogo(
                            nombre = extra.nombre,
                            foto = null,
                            modifier = Modifier
                                .border(
                                    if (elegido) 3.dp else 0.dp,
                                    if (elegido) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.onAgregadoClick(extra.nombre) }
                        ) {
                            Text(if (elegido) "✓ Seleccionado" else "Tocar para elegir", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                MensajeError(estado.errorAgregados)

                Spacer(Modifier.height(12.dp))
                Text(
                    "Elige tu ensalada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (ensaladas.isEmpty()) Text("No hay ensaladas disponibles hoy.")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(ensaladas.size) { indice ->
                        val extra = ensaladas[indice]
                        val elegido = estado.ensalada == extra.nombre
                        TarjetaCatalogo(
                            nombre = extra.nombre,
                            foto = null,
                            modifier = Modifier
                                .border(
                                    if (elegido) 3.dp else 0.dp,
                                    if (elegido) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.onEnsaladaClick(extra.nombre) }
                        ) {
                            Text(if (elegido) "✓ Seleccionada" else "Tocar para elegir", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                MensajeError(estado.errorEnsalada)

                Spacer(Modifier.height(12.dp))
                Text(
                    "Cantidad",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(onClick = { viewModel.onCantidadChange(-1) }) {
                        Text("-", color = MaterialTheme.colorScheme.onBackground)
                    }
                    Text("${estado.cantidad}", style = MaterialTheme.typography.titleMedium)
                    OutlinedButton(onClick = { viewModel.onCantidadChange(1) }) {
                        Text("+", color = MaterialTheme.colorScheme.onBackground)
                    }
                }

                Spacer(Modifier.height(12.dp))
                Text(
                    "Subtotal: ${(plato.precio * estado.cantidad).clp()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                BotonJoJo("Agregar al carrito", viewModel::onEnviar, Modifier.fillMaxWidth())
            }
        }
    }
}
