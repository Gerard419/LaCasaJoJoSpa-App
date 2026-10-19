package com.app.lacasajojo.ui.views.cliente

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.app.lacasajojo.ui.views.components.BarraSuperior
import com.app.lacasajojo.ui.views.components.BotonJoJo
import com.app.lacasajojo.ui.views.components.FotoPlato
import com.app.lacasajojo.ui.views.components.MensajeError
import com.app.lacasajojo.utils.clp
import cl.dsy1105.lacasajojo.viewmodel.AppViewModelProvider
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
                FotoPlato(
                    ruta = plato.foto,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
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
                agregados.forEach { extra ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onAgregadoClick(extra.nombre) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = extra.nombre in estado.agregados,
                            onCheckedChange = { viewModel.onAgregadoClick(extra.nombre) }
                        )
                        Text(extra.nombre)
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
                ensaladas.forEach { extra ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.onEnsaladaClick(extra.nombre) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = estado.ensalada == extra.nombre,
                            onClick = { viewModel.onEnsaladaClick(extra.nombre) }
                        )
                        Text(extra.nombre)
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
