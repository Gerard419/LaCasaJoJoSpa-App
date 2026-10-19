package com.app.lacasajojo.ui.views.admin

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.dsy1105.lacasajojo.ui.components.BarraSuperior
import cl.dsy1105.lacasajojo.ui.components.BotonJoJo
import cl.dsy1105.lacasajojo.ui.components.CampoTexto
import cl.dsy1105.lacasajojo.ui.components.TarjetaJoJo
import cl.dsy1105.lacasajojo.utils.MAX_EMAIL
import cl.dsy1105.lacasajojo.utils.MAX_NOMBRE
import cl.dsy1105.lacasajojo.utils.MAX_PASSWORD
import com.app.lacasajojo.viewmodel.AdminViewModel
import cl.dsy1105.lacasajojo.viewmodel.AppViewModelProvider

// Administrador: mantiene la app (crea cuentas de dueño y bloquea o activa usuarios)
@Composable
fun AdminScreen(viewModel: AdminViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    val estado by viewModel.estado.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()

    Scaffold(
        topBar = { BarraSuperior("Administración", "Salir") { viewModel.onSalir() } }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Column {
                    Text(
                        "Crear cuenta de dueño",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(8.dp))
                    CampoTexto(
                        valor = estado.nombre,
                        onCambio = viewModel::onNombreChange,
                        etiqueta = "Nombre",
                        icono = Icons.Filled.Person,
                        error = estado.errorNombre,
                        maxLength = MAX_NOMBRE
                    )
                    Spacer(Modifier.height(8.dp))
                    CampoTexto(
                        valor = estado.email,
                        onCambio = viewModel::onEmailChange,
                        etiqueta = "Correo electrónico",
                        icono = Icons.Filled.Email,
                        error = estado.errorEmail,
                        teclado = KeyboardType.Email,
                        maxLength = MAX_EMAIL
                    )
                    Spacer(Modifier.height(8.dp))
                    CampoTexto(
                        valor = estado.password,
                        onCambio = viewModel::onPasswordChange,
                        etiqueta = "Contraseña",
                        icono = Icons.Filled.Lock,
                        error = estado.errorPassword,
                        esPassword = true,
                        maxLength = MAX_PASSWORD
                    )
                    Spacer(Modifier.height(8.dp))
                    estado.mensajeExito?.let {
                        Text(it, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                    }
                    BotonJoJo("Crear dueño", viewModel::onEnviar, Modifier.fillMaxWidth())
                }
            }
            item {
                Text(
                    "Usuarios registrados",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            items(usuarios, key = { it.email }) { usuario ->
                TarjetaJoJo(modifier = Modifier.animateItem()) {
                    Text(usuario.nombre, fontWeight = FontWeight.Bold)
                    Text(usuario.email)
                    if (!usuario.telefono.isNullOrBlank()) Text("Teléfono: ${usuario.telefono}")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Rol: ${usuario.rol.texto}")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (usuario.activo) "Activa  " else "Bloqueada  ")
                            Switch(
                                checked = usuario.activo,
                                onCheckedChange = { viewModel.onCambiarEstadoUsuario(usuario.email) },
                                enabled = usuario.email != viewModel.emailActual
                            )
                        }
                    }
                }
            }
        }
    }
}//