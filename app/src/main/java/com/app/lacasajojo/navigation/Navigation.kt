package com.app.lacasajojo.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.app.lacasajojo.repository.AppRepository
import com.app.lacasajojo.model.Rol
import com.app.lacasajojo.ui.views.LoginScreen
import com.app.lacasajojo.ui.views.RegistroScreen
import com.app.lacasajojo.ui.views.admin.AdminScreen
import com.app.lacasajojo.ui.views.cliente.ArmarPlatoScreen
import com.app.lacasajojo.ui.views.cliente.BoletaScreen
import com.app.lacasajojo.ui.views.cliente.ClienteScreen
import com.app.lacasajojo.ui.views.dueno.DuenoScreen
import com.app.lacasajojo.ui.views.dueno.PlatoFormScreen

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val ADMIN = "admin"
    const val DUENO = "dueno"
    const val CLIENTE = "cliente"
    const val PLATO_FORM = "plato_form/{id}"
    const val ARMAR = "armar/{platoId}"
    const val BOLETA = "boleta/{numero}"

    fun platoForm(id: Int) = "plato_form/$id"
    fun armar(platoId: Int) = "armar/$platoId"
    fun boleta(numero: Int) = "boleta/$numero"

    fun inicioPara(rol: Rol): String = when (rol) {
        Rol.ADMIN -> ADMIN
        Rol.DUENO -> DUENO
        Rol.CLIENTE -> CLIENTE
    }
}

fun NavHostController.irALogin() {
    navigate(Rutas.LOGIN) {
        popUpTo(graph.id) { inclusive = true }
    }
}

// Control de acceso por rol (RNF-03): si no hay sesión o el rol no corresponde, vuelve al login.
// También se encarga de volver al login cuando se cierra la sesión.
@Composable
private fun Protegida(
    rol: Rol,
    navController: NavHostController,
    repository: AppRepository,
    contenido: @Composable () -> Unit
) {
    if (repository.usuarioActual?.rol == rol) {
        contenido()
    } else {
        LaunchedEffect(Unit) { navController.irALogin() }
    }
}

@Composable
fun AppNavigation(navController: NavHostController, repository: AppRepository) {
    // Transiciones entre pantallas: deslizan hacia los lados con desvanecimiento
    NavHost(
        navController = navController,
        startDestination = Rutas.LOGIN,
        enterTransition = { slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn(tween(300)) },
        exitTransition = { slideOutHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeOut(tween(300)) },
        popEnterTransition = { slideInHorizontally(animationSpec = tween(300)) { -it / 3 } + fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut(tween(300)) }
    ) {
        composable(Rutas.LOGIN) { LoginScreen(navController) }
        composable(Rutas.REGISTRO) { RegistroScreen(navController) }

        composable(Rutas.ADMIN) {
            Protegida(Rol.ADMIN, navController, repository) { AdminScreen() }
        }

        composable(Rutas.DUENO) {
            Protegida(Rol.DUENO, navController, repository) { DuenoScreen(navController) }
        }
        composable(
            Rutas.PLATO_FORM,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            val id = entrada.arguments?.getInt("id") ?: 0
            Protegida(Rol.DUENO, navController, repository) { PlatoFormScreen(navController, id) }
        }

        composable(Rutas.CLIENTE) {
            Protegida(Rol.CLIENTE, navController, repository) { ClienteScreen(navController) }
        }
        composable(
            Rutas.ARMAR,
            arguments = listOf(navArgument("platoId") { type = NavType.IntType })
        ) { entrada ->
            val platoId = entrada.arguments?.getInt("platoId") ?: 0
            Protegida(Rol.CLIENTE, navController, repository) { ArmarPlatoScreen(navController, platoId) }
        }

        // La boleta la ven el cliente dueño del pedido y el dueño del local
        composable(
            Rutas.BOLETA,
            arguments = listOf(navArgument("numero") { type = NavType.IntType })
        ) { entrada ->
            val numero = entrada.arguments?.getInt("numero") ?: 0
            BoletaScreen(navController, repository, numero)
        }
    }
}
