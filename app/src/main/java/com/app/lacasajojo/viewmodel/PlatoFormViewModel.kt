package com.app.lacasajojo.viewmodel

import androidx.lifecycle.ViewModel
import com.app.lacasajojo.model.Plato
import com.app.lacasajojo.repository.AppRepository
import com.app.lacasajojo.utils.validarMonto
import com.app.lacasajojo.utils.validarNombrePlato
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlatoFormUiState(
    val esNuevo: Boolean = true,
    val nombre: String = "",
    val precio: String = "",
    val foto: String? = null,
    val errorNombre: String? = null,
    val errorPrecio: String? = null,
    val mensajeCamara: String? = null,   // aviso si la cámara no se pudo usar
    val permisoDenegado: Boolean = false, // muestra el botón para abrir los ajustes
    val guardado: Boolean = false
)

// Crear o editar un plato (id = 0 significa plato nuevo)
class PlatoFormViewModel(
    private val repository: AppRepository,
    id: Int
) : ViewModel() {

    private val existente: Plato? = repository.platos.find { it.id == id }

    private val _estado = MutableStateFlow(
        PlatoFormUiState(
            esNuevo = existente == null,
            nombre = existente?.nombre ?: "",
            precio = existente?.precio?.toString() ?: "",
            foto = existente?.foto
        )
    )
    val estado: StateFlow<PlatoFormUiState> = _estado.asStateFlow()

    fun onNombreChange(valor: String) {
        _estado.update { it.copy(nombre = valor, errorNombre = validarNombrePlato(valor)) }
    }

    fun onPrecioChange(valor: String) {
        _estado.update { it.copy(precio = valor, errorPrecio = validarMonto(valor)) }
    }

    // ---- Cámara: el resultado se usa guardando la foto junto al plato
    fun onFotoTomada(ruta: String?) {
        _estado.update {
            if (ruta == null) {
                it.copy(mensajeCamara = "No se pudo guardar la foto. Puedes guardar el plato sin foto.")
            } else {
                it.copy(foto = ruta, mensajeCamara = null, permisoDenegado = false)
            }
        }
    }

    fun onPermisoCamaraDenegado() {
        _estado.update {
            it.copy(
                mensajeCamara = "Sin permiso de cámara no se puede sacar la foto. El plato se puede guardar igual.",
                permisoDenegado = true
            )
        }
    }

    fun onCamaraNoDisponible() {
        _estado.update { it.copy(mensajeCamara = "Este dispositivo no tiene cámara. El plato se puede guardar sin foto.") }
    }

    fun onEnviar() {
        val e = _estado.value
        val errorNombre = validarNombrePlato(e.nombre)
        val errorPrecio = validarMonto(e.precio)
        if (errorNombre != null || errorPrecio != null) {
            _estado.update { it.copy(errorNombre = errorNombre, errorPrecio = errorPrecio) }
            return
        }
        repository.guardarPlato(
            Plato(
                id = existente?.id ?: 0,
                nombre = e.nombre.trim(),
                precio = e.precio.trim().toInt(),
                disponible = existente?.disponible ?: true,
                foto = e.foto
            )
        )
        _estado.update { it.copy(guardado = true) }
    }
}
