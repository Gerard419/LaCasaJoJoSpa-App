package com.app.lacasajojo.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Fondo blanco, letras negras y botones naranjos (sin colores dinámicos, para mantener la identidad)
private val EsquemaClaro = lightColorScheme(
    primary = Naranjo,
    onPrimary = Negro,
    primaryContainer = NaranjoClaro,
    onPrimaryContainer = Negro,
    secondary = Naranjo,
    onSecondary = Negro,
    background = Blanco,
    onBackground = Negro,
    surface = Blanco,
    onSurface = Negro,
    surfaceVariant = NaranjoMuyClaro,
    onSurfaceVariant = Negro,
    outline = NaranjoOscuro
)

@Composable
fun LaCasaJoJoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography = Typography,
        content = content
    )
}
