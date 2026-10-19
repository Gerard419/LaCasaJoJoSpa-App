package com.app.lacasajojo.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.app.lacasajojo.ui.theme.NaranjoClaro
import com.app.lacasajojo.ui.theme.NaranjoMuyClaro
import com.app.lacasajojo.ui.theme.NaranjoOscuro
import com.app.lacasajojo.ui.theme.VerdeOk
import com.app.lacasajojo.utils.MAX_NOMBRE
import com.app.lacasajojo.utils.cargarFoto

// Barra naranja con título, botón "Volver" opcional y una acción opcional (ej.: Salir)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    textoAccion: String? = null,
    onVolver: (() -> Unit)? = null,
    onAccion: () -> Unit = {}
) {
    val colorTexto = MaterialTheme.colorScheme.onPrimary
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = colorTexto
        ),
        title = { Text(titulo, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            if (onVolver != null) {
                TextButton(
                    onClick = onVolver,
                    colors = ButtonDefaults.textButtonColors(contentColor = colorTexto)
                ) { Text("Volver") }
            }
        },
        actions = {
            if (textoAccion != null) {
                TextButton(
                    onClick = onAccion,
                    colors = ButtonDefaults.textButtonColors(contentColor = colorTexto)
                ) { Text(textoAccion) }
            }
        }
    )
}

// Pestañas con TabRow: la seleccionada va en negrita y con el indicador naranjo animado
@Composable
fun Pestanas(opciones: List<String>, seleccionada: Int, onSeleccion: (Int) -> Unit) {
    val colorTexto = MaterialTheme.colorScheme.onBackground
    TabRow(
        selectedTabIndex = seleccionada,
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = colorTexto
    ) {
        opciones.forEachIndexed { i, texto ->
            Tab(
                selected = i == seleccionada,
                onClick = { onSeleccion(i) },
                selectedContentColor = colorTexto,
                unselectedContentColor = colorTexto.copy(alpha = 0.7f),
                text = {
                    Text(
                        texto,
                        maxLines = 1,
                        fontWeight = if (i == seleccionada) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }
    }
}

@Composable
fun TarjetaJoJo(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NaranjoMuyClaro),
        border = BorderStroke(1.dp, NaranjoClaro)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = contenido
        )
    }
}

// Contenedor de formularios: centrado y con ancho máximo (sirve en horizontal y en tablets)
@Composable
fun ColumnaFormulario(
    padding: PaddingValues,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    contenido: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = verticalArrangement,
            horizontalAlignment = horizontalAlignment,
            content = contenido
        )
    }
}

// Campo con ícono de entrada, ícono de error o correcto, ojo para la contraseña
// y mensaje de error propio que aparece con animación.
@Composable
fun CampoTexto(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    modifier: Modifier = Modifier,
    error: String? = null,
    esPassword: Boolean = false,
    teclado: KeyboardType = KeyboardType.Text,
    maxLength: Int = MAX_NOMBRE,
    singleLine: Boolean = true
) {
    var verPassword by remember { mutableStateOf(false) }
    var ultimoError by remember { mutableStateOf("") }
    LaunchedEffect(error) { if (error != null) ultimoError = error }

    val esValido = error == null && valor.isNotBlank()
    val colorError = MaterialTheme.colorScheme.error
    val colorNormal = MaterialTheme.colorScheme.onBackground
    val colorIcono by animateColorAsState(
        targetValue = when {
            error != null -> colorError
            esValido -> VerdeOk
            else -> colorNormal
        },
        label = "colorIconoCampo"
    )

    OutlinedTextField(
        value = valor,
        onValueChange = { if (it.length <= maxLength) onCambio(it) },
        label = { Text(etiqueta) },
        singleLine = singleLine,
        isError = error != null,
        leadingIcon = { Icon(icono, contentDescription = "Ícono del campo $etiqueta", tint = colorIcono) },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (error != null) {
                    Icon(Icons.Filled.Error, contentDescription = "El campo $etiqueta tiene un error", tint = colorError)
                } else if (esValido) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "El campo $etiqueta es correcto", tint = VerdeOk)
                }
                if (esPassword) {
                    IconButton(onClick = { verPassword = !verPassword }) {
                        Icon(
                            imageVector = if (verPassword) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (verPassword) "Ocultar contraseña" else "Mostrar contraseña"
                        )
                    }
                }
            }
        },
        supportingText = {
            AnimatedVisibility(
                visible = error != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(error ?: ultimoError)
            }
        },
        visualTransformation = if (esPassword && !verPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (esPassword) KeyboardType.Password else teclado),
        modifier = modifier.fillMaxWidth()
    )
}

// Botón naranja que se achica y se oscurece al presionarlo
@Composable
fun BotonJoJo(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(targetValue = if (presionado) 0.96f else 1f, label = "escalaBoton")
    val color by animateColorAsState(
        targetValue = if (presionado) NaranjoOscuro else MaterialTheme.colorScheme.primary,
        label = "colorBoton"
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaccion,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        modifier = modifier.scale(escala)
    ) { Text(texto) }
}

// Botón de texto legible sobre fondo blanco (negro, o rojo si es peligroso)
@Composable
fun BotonTexto(texto: String, peligro: Boolean = false, onClick: () -> Unit) {
    val color = if (peligro) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground
    TextButton(onClick = onClick, colors = ButtonDefaults.textButtonColors(contentColor = color)) {
        Text(texto)
    }
}

// Mensaje de error general (no de un campo) que aparece y desaparece suavemente
@Composable
fun MensajeError(texto: String?) {
    AnimatedVisibility(
        visible = !texto.isNullOrEmpty(),
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Text(texto.orEmpty(), color = MaterialTheme.colorScheme.error)
    }
}

// Foto de un plato guardada por la cámara; si no hay foto no dibuja nada
@Composable
fun FotoPlato(ruta: String?, modifier: Modifier = Modifier) {
    val imagen = remember(ruta) { cargarFoto(ruta) }
    if (imagen != null) {
        Image(
            bitmap = imagen,
            contentDescription = "Foto del plato",
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
