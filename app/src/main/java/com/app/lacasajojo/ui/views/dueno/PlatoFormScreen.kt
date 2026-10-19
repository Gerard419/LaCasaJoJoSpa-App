package com.app.lacasajojo.ui.views.dueno

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.app.lacasajojo.ui.components.BarraSuperior
import com.app.lacasajojo.ui.components.BotonJoJo
import com.app.lacasajojo.ui.components.BotonTexto
import com.app.lacasajojo.ui.components.CampoTexto
import com.app.lacasajojo.ui.components.ColumnaFormulario
import com.app.lacasajojo.ui.components.FotoPlato
import com.app.lacasajojo.utils.MAX_PLATO
import com.app.lacasajojo.utils.guardarFoto
import com.app.lacasajojo.viewmodel.AppViewModelProvider
import com.app.lacasajojo.viewmodel.PlatoFormViewModel

// Crear o editar un plato (id = 0 significa plato nuevo).
// Recurso nativo: cámara para sacarle una foto al plato (el permiso se pide al tocar el botón).
@Composable
fun PlatoFormScreen(navController: NavHostController, id: Int) {
    val viewModel: PlatoFormViewModel = viewModel(
        key = "plato_form_$id",
        factory = AppViewModelProvider.platoForm(id)
    )
    val estado by viewModel.estado.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(estado.guardado) {
        if (estado.guardado) navController.popBackStack()
    }

    // Abre la cámara y guarda la foto (miniatura) en el almacenamiento interno de la app
    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        if (bitmap != null) viewModel.onFotoTomada(guardarFoto(context, bitmap))
    }

    fun lanzarCamara() {
        try {
            tomarFoto.launch(null)
        } catch (e: ActivityNotFoundException) {
            viewModel.onCamaraNoDisponible()
        }
    }

    // Respuesta del diálogo de permiso: si lo rechaza, se avisa y la pantalla sigue funcionando
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) lanzarCamara() else viewModel.onPermisoCamaraDenegado()
    }

    fun abrirCamara() {
        if (!context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)) {
            viewModel.onCamaraNoDisponible()
        } else if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            lanzarCamara()
        } else {
            pedirPermiso.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                if (estado.esNuevo) "Nuevo plato" else "Editar plato",
                onVolver = { navController.popBackStack() }
            )
        }
    ) { padding ->
        ColumnaFormulario(padding = padding) {
            CampoTexto(
                valor = estado.nombre,
                onCambio = viewModel::onNombreChange,
                etiqueta = "Nombre del plato",
                icono = Icons.Filled.Restaurant,
                error = estado.errorNombre,
                maxLength = MAX_PLATO
            )
            Spacer(Modifier.height(8.dp))
            CampoTexto(
                valor = estado.precio,
                onCambio = viewModel::onPrecioChange,
                etiqueta = "Precio en pesos (ej.: 4800)",
                icono = Icons.Filled.AttachMoney,
                error = estado.errorPrecio,
                teclado = KeyboardType.Number,
                maxLength = 6
            )
            Spacer(Modifier.height(16.dp))

            // Foto del plato (opcional)
            FotoPlato(
                ruta = estado.foto,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            )
            if (estado.foto != null) Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = ::abrirCamara, modifier = Modifier.fillMaxWidth()) {
                Icon(
                    Icons.Filled.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    if (estado.foto == null) "Sacar foto del plato" else "Cambiar foto",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            AnimatedVisibility(
                visible = estado.mensajeCamara != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(estado.mensajeCamara.orEmpty(), color = MaterialTheme.colorScheme.error)
            }
            // Si lo rechazó con "no volver a preguntar", solo se puede activar desde los ajustes
            if (estado.permisoDenegado) {
                BotonTexto("Abrir ajustes de la app") {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            BotonJoJo("Guardar", viewModel::onEnviar, Modifier.fillMaxWidth())
        }
    }
}
