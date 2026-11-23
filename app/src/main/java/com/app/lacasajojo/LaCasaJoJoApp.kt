package com.app.lacasajojo

import android.app.Application
import com.app.lacasajojo.data.DataStoreManager
import com.app.lacasajojo.remote.ApiConfig
import com.app.lacasajojo.repository.AppRepository
import com.app.lacasajojo.repository.ClimaRepository

// Crea un único repositorio para toda la app; los ViewModel lo reciben desde aquí
class LaCasaJoJoApp : Application() {
    val repository: AppRepository by lazy {
        AppRepository(DataStoreManager(applicationContext), ApiConfig.crearBackend()).also {
            it.cargarDatos()
            it.iniciarSincronizacion()
        }
    }

    // API externa (clima)
    val climaRepository: ClimaRepository by lazy { ClimaRepository(ApiConfig.crearClimaApi()) }
}
