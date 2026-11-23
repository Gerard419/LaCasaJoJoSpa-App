package com.app.lacasajojo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.app.lacasajojo.ui.theme.NaranjoOscuro
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.lacasajojo.R
import com.app.lacasajojo.ui.theme.NaranjoClaro
import com.app.lacasajojo.ui.theme.NaranjoMuyClaro

/**
 * Presentación del menú del video anterior: imagen sobre tarjeta crema.
 * Si el dueño sube una foto, se utiliza siempre la del servidor.
 * Las imágenes de referencia del video se usan solo cuando el plato todavía no tiene foto.
 */
@Composable
fun FotoCatalogo(nombre: String, ruta: String?, modifier: Modifier = Modifier) {
    val imagenLocal = when {
        nombre.contains("guatitas", ignoreCase = true) -> R.drawable.comida_guatitas
        nombre.contains("cerdo mongoliano", ignoreCase = true) -> R.drawable.comida_cerdo
        nombre.contains("filete de pollo", ignoreCase = true) -> R.drawable.comida_filete
        nombre.contains("tortilla de porotos", ignoreCase = true) -> R.drawable.comida_tortilla
        nombre.contains("pollo asado", ignoreCase = true) -> R.drawable.comida_pollo
        nombre.contains("puré", ignoreCase = true) -> R.drawable.comida_pure
        nombre.contains("arroz normal", ignoreCase = true) -> R.drawable.comida_arroz
        nombre.contains("mix de ensaladas", ignoreCase = true) -> R.drawable.comida_ensalada
        else -> null
    }
    Box(modifier = modifier.background(NaranjoClaro), contentAlignment = Alignment.Center) {
        if (ruta.isNullOrBlank()) {
            if (imagenLocal != null) {
                Image(
                    painter = painterResource(imagenLocal),
                    contentDescription = "Imagen ilustrativa de $nombre",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = "Sin foto para $nombre",
                    tint = Color(0xFF98621A),
                    modifier = Modifier.padding(25.dp)
                )
            }
        } else {
            // Estado visual mientras se descarga la foto (o si la red está caída).
            Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                tint = Color(0xFF98621A),
                modifier = Modifier.padding(25.dp)
            )
            // La imagen subida por el dueño tiene prioridad sobre toda ilustración local.
            FotoPlato(ruta = ruta, modifier = Modifier.fillMaxSize())
        }
    }
}

/** Tarjeta horizontal compacta, conservando botones y controles que defina cada pantalla. */
@Composable
fun TarjetaCatalogo(
    nombre: String,
    foto: String?,
    modifier: Modifier = Modifier,
    ancho: Dp = 172.dp,
    precio: String? = null,
    contenido: @Composable ColumnScope.() -> Unit = {}
) {
    Card(
        modifier = modifier.width(ancho),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = NaranjoMuyClaro),
        border = androidx.compose.foundation.BorderStroke(1.dp, NaranjoClaro),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FotoCatalogo(
                nombre = nombre,
                ruta = foto,
                modifier = Modifier.fillMaxWidth().height(118.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            )
            Column(
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    nombre,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    minLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (precio != null) {
                    Text(
                        precio,
                        style = MaterialTheme.typography.titleSmall,
                        color = NaranjoOscuro,
                        fontWeight = FontWeight.Bold
                    )
                }
                contenido()
            }
        }
    }
}
