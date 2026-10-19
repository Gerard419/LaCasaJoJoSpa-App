package com.app.lacasajojo.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

// ----------------------------------------------------------------------
// Recurso nativo 1: cámara (guardar la foto en el almacenamiento interno de la app)
// ----------------------------------------------------------------------

// Devuelve la ruta del archivo guardado, o null si falló
fun guardarFoto(context: Context, bitmap: Bitmap): String? {
    return try {
        val carpeta = File(context.filesDir, "platos").apply { mkdirs() }
        val archivo = File(carpeta, "plato_${System.currentTimeMillis()}.jpg")
        FileOutputStream(archivo).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        archivo.absolutePath
    } catch (e: IOException) {
        null
    }
}

fun cargarFoto(ruta: String?): ImageBitmap? {
    if (ruta.isNullOrBlank() || !File(ruta).exists()) return null
    return BitmapFactory.decodeFile(ruta)?.asImageBitmap()
}

// ----------------------------------------------------------------------
// Recurso nativo 2: vibración (confirmar pedido y errores de ingreso)
// El permiso VIBRATE es "normal": se concede al instalar, no hay que pedirlo en pantalla.
// ----------------------------------------------------------------------
@Suppress("DEPRECATION")
private fun obtenerVibrador(context: Context): Vibrator? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }
}

@Suppress("DEPRECATION")
private fun vibrarSinEfecto(vibrador: Vibrator, milisegundos: Long) {
    vibrador.vibrate(milisegundos) // Android 7.x (API 24 y 25) no tiene VibrationEffect
}

fun vibrar(context: Context, milisegundos: Long = 80) {
    try {
        val vibrador = obtenerVibrador(context) ?: return
        if (!vibrador.hasVibrator()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrador.vibrate(
                VibrationEffect.createOneShot(milisegundos, VibrationEffect.DEFAULT_AMPLITUDE)
            )
        } else {
            vibrarSinEfecto(vibrador, milisegundos)
        }
    } catch (e: SecurityException) {
        // Sin permiso de vibración: la app sigue funcionando sin vibrar
    }
}
