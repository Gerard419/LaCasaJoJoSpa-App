package com.app.lacasajojo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.app.lacasajojo.navigation.AppNavigation
import com.app.lacasajojo.repository.AppRepository
import com.app.lacasajojo.ui.theme.LaCasaJoJoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repository = (application as LaCasaJoJoApp).repository
        setContent {
            MyApp(repository)
        }
    }
}

@Composable
fun MyApp(repository: AppRepository) {
    val navController = rememberNavController()
    val context = LocalContext.current

    // Avisos de red (sin conexión, error del servidor...) como mensaje emergente
    LaunchedEffect(repository) {
        snapshotFlow { repository.mensajeRed }.collect { mensaje ->
            if (mensaje != null) {
                Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                repository.limpiarMensajeRed()
            }
        }
    }
    LaCasaJoJoTheme {
        AppNavigation(navController, repository)
    }
}
