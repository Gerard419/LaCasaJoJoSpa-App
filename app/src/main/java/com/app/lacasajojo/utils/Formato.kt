package com.app.lacasajojo.utils

// 4600 -> "$4.600"
fun Int.clp(): String {
    return "\$" + this.toString().reversed().chunked(3).joinToString(".").reversed()
}

// 12 -> "000012"
fun Int.formatoBoleta(): String {
    return this.toString().padStart(6, '0')
}
