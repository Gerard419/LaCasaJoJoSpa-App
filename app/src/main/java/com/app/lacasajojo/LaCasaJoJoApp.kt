package com.app.lacasajojo

import android.app.Application
import cl.dsy1105.lacasajojo.data.DataStoreManager
import com.app.lacasajojo.repository.AppRepository

// Crea un único repositorio para toda la app; los ViewModel lo reciben desde aquí
class LaCasaJoJoApp : Application() {
    val repository: AppRepository by lazy {
        AppRepository(DataStoreManager(applicationContext)).also { it.cargarDatos() }
    }
}
