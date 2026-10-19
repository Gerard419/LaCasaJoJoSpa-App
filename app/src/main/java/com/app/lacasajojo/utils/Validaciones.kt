package com.app.lacasajojo.utils

import java.security.MessageDigest

// Largos máximos de los campos de texto
const val MAX_NOMBRE = 40
const val MAX_EMAIL = 60
const val MAX_PLATO = 40
const val MAX_PASSWORD = 30
const val MAX_OBSERVACIONES = 200
const val LARGO_TELEFONO = 9

private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

fun esEmailValido(email: String): Boolean {
    return EMAIL_REGEX.matches(email.trim())
}

// RNF-05: mínimo 8 caracteres, con al menos una letra y un número
fun esPasswordValida(password: String): Boolean {
    return password.length >= 8 && password.any { it.isLetter() } && password.any { it.isDigit() }
}

// RNF-01: la contraseña no se guarda en texto plano
fun hashPassword(password: String): String {
    return MessageDigest.getInstance("SHA-256")
        .digest(password.toByteArray())
        .joinToString("") { "%02x".format(it) }
}

// ----------------------------------------------------------------------
// Validadores con mensaje: devuelven null si el valor es válido o el texto del error.
// Las pantallas no validan nada; solo muestran lo que decide el ViewModel con estas funciones.
// ----------------------------------------------------------------------

fun validarNombre(nombre: String): String? = when {
    nombre.isBlank() -> "El nombre es obligatorio"
    nombre.trim().length < 2 -> "El nombre debe tener al menos 2 caracteres"
    nombre.length > MAX_NOMBRE -> "El nombre no puede superar los $MAX_NOMBRE caracteres"
    nombre.any { it.isDigit() } -> "El nombre no puede contener números"
    else -> null
}

fun validarEmail(email: String): String? = when {
    email.isBlank() -> "El correo es obligatorio"
    email.length > MAX_EMAIL -> "El correo no puede superar los $MAX_EMAIL caracteres"
    !esEmailValido(email) -> "El formato del correo no es válido"
    else -> null
}

fun validarTelefono(telefono: String): String? = when {
    telefono.isBlank() -> "El teléfono es obligatorio"
    telefono.any { !it.isDigit() } -> "Usa solo números"
    telefono.length != LARGO_TELEFONO || !telefono.startsWith("9") ->
        "Debe tener $LARGO_TELEFONO dígitos y empezar con 9 (ej.: 912345678)"
    else -> null
}

fun validarPassword(password: String): String? = when {
    password.isBlank() -> "La contraseña es obligatoria"
    !esPasswordValida(password) -> "Mínimo 8 caracteres, con letras y números"
    else -> null
}

// Para el login solo se exige que no esté vacía (la regla completa aplica al crearla)
fun validarPasswordLogin(password: String): String? =
    if (password.isBlank()) "La contraseña es obligatoria" else null

fun validarConfirmacion(password: String, confirmar: String): String? = when {
    confirmar.isBlank() -> "Confirma tu contraseña"
    password != confirmar -> "Las contraseñas no coinciden"
    else -> null
}

fun validarNombrePlato(nombre: String): String? = when {
    nombre.isBlank() -> "El nombre es obligatorio"
    nombre.trim().length < 3 -> "El nombre debe tener al menos 3 caracteres"
    nombre.length > MAX_PLATO -> "El nombre no puede superar los $MAX_PLATO caracteres"
    else -> null
}

// El monto llega como texto desde el campo; solo números enteros mayores a 0
fun validarMonto(monto: String): String? {
    val valor = monto.trim().toIntOrNull()
    return when {
        monto.isBlank() -> "El precio es obligatorio"
        valor == null -> "Ingresa solo números (ej.: 4800)"
        valor <= 0 -> "El precio debe ser mayor a 0"
        valor > 100000 -> "El precio no puede superar \$100.000"
        else -> null
    }
}

// Regla del menú: 1 o 2 agregados y una ensalada obligatoria
fun validarAgregados(cantidad: Int): String? = when {
    cantidad < 1 -> "Debes elegir al menos 1 agregado"
    cantidad > 2 -> "Solo puedes elegir hasta 2 agregados"
    else -> null
}

fun validarEnsalada(ensalada: String): String? =
    if (ensalada.isBlank()) "Debes elegir una ensalada" else null
