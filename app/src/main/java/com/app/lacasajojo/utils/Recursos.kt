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
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import com.app.lacasajojo.remote.ApiConfig

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

// Las fotos ya publicadas usan una ruta relativa del backend; las no publicadas usan archivo local.
// Se ejecuta fuera del hilo principal para no bloquear Compose durante la descarga.
suspend fun cargarFoto(ruta: String?): ImageBitmap? = withContext(Dispatchers.IO) {
    if (ruta.isNullOrBlank()) return@withContext null
    try {
        val bitmap = if (ruta.startsWith("/api/fotos/")) {
            // Nunca aceptar URLs arbitrarias del backend: solo la ruta de fotografías propia.
            if (!Regex("^/api/fotos/[0-9a-fA-F-]{36}\\.jpg$").matches(ruta)) return@withContext null
            val conexion = (URL("http://${ApiConfig.HOST}:${ApiConfig.PUERTO_MENU}$ruta")
                .openConnection() as HttpURLConnection)
            try {
                conexion.connectTimeout = 4000
                conexion.readTimeout = 8000
                if (conexion.responseCode != 200) return@withContext null
                conexion.inputStream.use { BitmapFactory.decodeStream(it) }
            } finally {
                conexion.disconnect()
            }
        } else {
            BitmapFactory.decodeFile(ruta)
        }
        bitmap?.asImageBitmap()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }
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
