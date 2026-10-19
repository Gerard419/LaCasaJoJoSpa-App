package com.app.lacasajojo.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.app.lacasajojo.navigation.Rutas
import com.app.lacasajojo.ui.components.BarraSuperior
import com.app.lacasajojo.ui.components.BotonJoJo
import com.app.lacasajojo.ui.components.BotonTexto
import com.app.lacasajojo.ui.components.CampoTexto
import com.app.lacasajojo.ui.components.ColumnaFormulario
import com.app.lacasajojo.ui.components.MensajeError
import com.app.lacasajojo.utils.MAX_EMAIL
import com.app.lacasajojo.utils.MAX_PASSWORD
import com.app.lacasajojo.utils.vibrar
import com.app.lacasajojo.viewmodel.AppViewModelProvider
import com.app.lacasajojo.viewmodel.LoginViewModel

// Pantalla de ingreso: la usan los 3 tipos de usuario
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val estado by viewModel.estado.collectAsState()
    val cargado by viewModel.cargado.collectAsState()
    val context = LocalContext.current

    // Ingreso correcto: navega al inicio del rol
    LaunchedEffect(estado.rolIngresado) {
        val rol = estado.rolIngresado ?: return@LaunchedEffect
        navController.navigate(Rutas.inicioPara(rol)) {
            popUpTo(Rutas.LOGIN) { inclusive = true }
        }
        viewModel.onIngresoProcesado()
    }

    // Ingreso fallido: vibración corta como retroalimentación (recurso nativo)
    LaunchedEffect(estado.errorGeneral) {
        if (estado.errorGeneral != null) vibrar(context, 60)
    }

    Scaffold(topBar = { BarraSuperior("La casa JoJo") }) { padding ->
        ColumnaFormulario(
            padding = padding,
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Arma tu menú",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text("Inicia sesión para continuar")
            Spacer(Modifier.height(24.dp))

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
            Spacer(Modifier.height(16.dp))

            MensajeError(estado.errorGeneral)

            BotonJoJo(
                texto = if (cargado) "Ingresar" else "Cargando...",
                onClick = viewModel::onEnviar,
                enabled = cargado,
                modifier = Modifier.fillMaxWidth()
            )
            BotonTexto("¿No tienes cuenta? Regístrate aquí") {
                navController.navigate(Rutas.REGISTRO)
            }
        }
    }
}