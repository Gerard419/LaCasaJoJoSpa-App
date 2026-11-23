package com.app.lacasajojo.utils

// Códigos WMO que entrega Open-Meteo -> texto en español
fun descripcionClima(codigo: Int): String = when (codigo) {
    0 -> "Despejado"
    1, 2, 3 -> "Parcialmente nublado"
    45, 48 -> "Neblina"
    in 51..57 -> "Llovizna"
    in 61..67 -> "Lluvia"
    in 71..77 -> "Nieve"
    in 80..82 -> "Chubascos"
    in 95..99 -> "Tormenta"
    else -> "Sin información"
}

// Mensaje corto para el cliente según el clima
fun sugerenciaPorClima(temperatura: Double, codigo: Int): String = when {
    codigo in 51..99 -> "Día de lluvia: pide tu plato para retirar y llévalo calentito."
    temperatura < 12 -> "Día frío: ideal un plato caliente de la casa."
    temperatura >= 25 -> "Día caluroso: acompaña tu plato con una ensalada fresca."
    else -> "Buen día para almorzar en La casa JoJo."
}
