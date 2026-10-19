package com.app.lacasajojo.ui.views

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.app.lacasajojo.ui.components.BarraSuperior
import com.app.lacasajojo.ui.components.BotonJoJo
import com.app.lacasajojo.ui.components.CampoTexto
import com.app.lacasajojo.ui.components.ColumnaFormulario
import com.app.lacasajojo.utils.LARGO_TELEFONO
import com.app.lacasajojo.utils.MAX_EMAIL
import com.app.lacasajojo.utils.MAX_NOMBRE
import com.app.lacasajojo.utils.MAX_PASSWORD
import com.app.lacasajojo.viewmodel.AppViewModelProvider
import com.app.lacasajojo.viewmodel.RegistroViewModel

// RF-01: registro público, solo para clientes
@Composable
fun RegistroScreen(
    navController: NavHostController,
    viewModel: RegistroViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado.registrado) {
        if (estado.registrado) navController.popBackStack() // vuelve al login
    }

    Scaffold(
        topBar = { BarraSuperior("Crear cuenta", onVolver = { navController.popBackStack() }) }
    ) { padding ->
        ColumnaFormulario(padding = padding) {
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
                valor = estado.telefono,
                onCambio = viewModel::onTelefonoChange,
                etiqueta = "Teléfono (ej.: 912345678)",
                icono = Icons.Filled.Phone,
                error = estado.errorTelefono,
                teclado = KeyboardType.Phone,
                maxLength = LARGO_TELEFONO
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
            CampoTexto(
                valor = estado.confirmar,
                onCambio = viewModel::onConfirmarChange,
                etiqueta = "Confirmar contraseña",
                icono = Icons.Filled.Lock,
                error = estado.errorConfirmar,
                esPassword = true,
                maxLength = MAX_PASSWORD
            )
            Spacer(Modifier.height(16.dp))

            BotonJoJo("Registrarse", viewModel::onEnviar, Modifier.fillMaxWidth())
        }
    }
}