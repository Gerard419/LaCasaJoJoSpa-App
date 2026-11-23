package com.app.lacasajojo.repository

import com.app.lacasajojo.remote.ApiConfig
import com.app.lacasajojo.remote.ClimaApi
import kotlin.coroutines.cancellation.CancellationException

data class Clima(val temperatura: Double, val viento: Double, val codigo: Int)

// API externa: si falla, la app sigue funcionando con sus datos propios (devuelve null)
class ClimaRepository(private val api: ClimaApi) {

    suspend fun obtenerClima(
        latitud: Double = ApiConfig.LATITUD,
        longitud: Double = ApiConfig.LONGITUD
    ): Clima? {
        return try {
            val actual = api.clima(latitud, longitud, true).climaActual ?: return null
            Clima(actual.temperature, actual.windspeed, actual.weathercode)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
    }
}
