package com.app.lacasajojo.ui.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.CircleShape
import com.app.lacasajojo.ui.theme.NaranjoOscuro
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
    val servidorDisponible by viewModel.servidorDisponible.collectAsState()
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

    Scaffold { padding ->
        ColumnaFormulario(
            padding = padding,
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(34.dp))
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                Icon(
                    Icons.Filled.RestaurantMenu,
                    contentDescription = null,
                    tint = NaranjoOscuro,
                    modifier = Modifier.size(72.dp).padding(17.dp)
                )
            }
            Spacer(Modifier.height(18.dp))
            Text(
                "Arma tu menú",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                "La casa JoJo · Inicia sesión para continuar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(28.dp))

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
            if (cargado && !servidorDisponible) {
                MensajeError("No hay conexión con el servidor. Revisa que los microservicios estén encendidos.")
            }

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