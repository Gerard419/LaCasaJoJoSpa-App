package com.app.lacasajojo.ui.views.cliente

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
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.app.lacasajojo.repository.AppRepository
import com.app.lacasajojo.model.Pedido
import com.app.lacasajojo.model.Rol
import com.app.lacasajojo.ui.components.BarraSuperior
import com.app.lacasajojo.ui.components.TarjetaJoJo
import com.app.lacasajojo.utils.clp
import com.app.lacasajojo.utils.formatoBoleta

// Boleta del pedido: detalle, neto, IVA y total (comprobante interno, no tributario)
@Composable
fun BoletaScreen(navController: NavHostController, repository: AppRepository, numero: Int) {
    val usuario = repository.usuarioActual
    val pedido: Pedido? = repository.pedidoPorNumero(numero)

    // Un cliente solo ve sus propias boletas; el dueño puede ver todas
    val puedeVer = pedido != null && usuario != null &&
        (usuario.rol == Rol.DUENO || pedido.emailCliente == usuario.email)

    Scaffold(
        topBar = { BarraSuperior("Boleta", onVolver = { navController.popBackStack() }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (!puedeVer || pedido == null) {
                Text("No tienes acceso a esta boleta.")
            } else {
                TarjetaJoJo {
                    Text(
                        "LA CASA JOJO",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Boleta N° ${pedido.numero.formatoBoleta()}", fontWeight = FontWeight.Bold)
                    Text("Fecha: ${pedido.fecha}")
                    Text("Cliente: ${pedido.nombreCliente}")
                    HorizontalDivider()

                    pedido.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${item.cantidad} x ${item.plato}",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Text(item.subtotal().clp(), fontWeight = FontWeight.Bold)
                        }
                        Text("   Agregados: ${item.agregados.joinToString(", ")}")
                        Text("   Ensalada: ${item.ensalada}")
                        Text("   Precio unitario: ${item.precioUnitario.clp()}")
                    }

                    if (pedido.observaciones.isNotEmpty()) {
                        HorizontalDivider()
                        Text("Observaciones: ${pedido.observaciones}")
                    }
                    HorizontalDivider()

                    FilaMonto("Neto", pedido.neto().clp())
                    FilaMonto("IVA (19 %)", pedido.iva().clp())
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("TOTAL", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            pedido.total().clp(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    HorizontalDivider()
                    Text("Estado: ${pedido.estado.texto}")
                }
                Spacer(Modifier.height(8.dp))
                Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Volver")
                }
            }
        }
    }
}

@Composable
private fun FilaMonto(etiqueta: String, monto: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta)
        Text(monto)
    }
}
