package com.app.lacasajojo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import cl.dsy1105.lacasajojo.navigation.AppNavigation
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
    LaCasaJoJoTheme {
        AppNavigation(navController, repository)
    }
}
