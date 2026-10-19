package com.app.lacasajojo.viewmodel



import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.lacasajojo.LaCasaJoJoApp

// Obtiene la Application para entregar el repositorio a cada ViewModel
fun CreationExtras.app(): LaCasaJoJoApp =
    this[AndroidViewModelFactory.APPLICATION_KEY] as LaCasaJoJoApp

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { LoginViewModel(app().repository) }
        initializer { RegistroViewModel(app().repository) }
        initializer { AdminViewModel(app().repository) }
        initializer { DuenoViewModel(app().repository) }
        initializer { ClienteViewModel(app().repository) }
    }

    // Estos dos reciben un parámetro de la ruta (id del plato)
    fun platoForm(id: Int) = viewModelFactory {
        initializer { PlatoFormViewModel(app().repository, id) }
    }

    fun armarPlato(platoId: Int) = viewModelFactory {
        initializer { ArmarPlatoViewModel(app().repository, platoId) }
    }
}
