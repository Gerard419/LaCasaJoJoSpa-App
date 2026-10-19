package com.app.lacasajojo.model

enum class Rol(val texto: String) {
    ADMIN("Administrador"),
    DUENO("Dueño"),
    CLIENTE("Cliente")
}

enum class TipoExtra { AGREGADO, ENSALADA }

enum class EstadoPedido(val texto: String) {
    RECIBIDO("Recibido"),
    EN_PREPARACION("En preparación"),
    LISTO("Listo para retiro"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado");

    fun siguiente(): EstadoPedido? = when (this) {
        RECIBIDO -> EN_PREPARACION
        EN_PREPARACION -> LISTO
        LISTO -> ENTREGADO
        else -> null
    }
}

data class Usuario(
    val nombre: String,
    val email: String,
    val passwordHash: String,
    val rol: Rol,
    val activo: Boolean = true,
    val telefono: String? = null // solo lo piden en el registro de clientes
)

// Plato principal (ej.: Pollo Asado, $4.800)
data class Plato(
    val id: Int,
    val nombre: String,
    val precio: Int,
    val disponible: Boolean = true, // "disponible hoy" = está en el menú del día
    val foto: String? = null // ruta de la foto tomada con la cámara (recurso nativo)
)

// Agregado (puré, arroz...) o ensalada
data class Extra(
    val id: Int,
    val nombre: String,
    val tipo: TipoExtra,
    val disponible: Boolean = true
)

data class ItemPedido(
    val plato: String,
    val precioUnitario: Int,
    val agregados: List<String>,
    val ensalada: String,
    val cantidad: Int
) {
    fun subtotal(): Int = precioUnitario * cantidad
}

data class Pedido(
    val numero: Int,
    val emailCliente: String,
    val nombreCliente: String,
    val fecha: String,
    val items: List<ItemPedido>,
    val observaciones: String,
    val estado: EstadoPedido
) {
    fun total(): Int = items.sumOf { it.subtotal() }

    // Los precios incluyen IVA (19 %): neto = total / 1,19
    fun neto(): Int = Math.round(total() / 1.19).toInt()
    fun iva(): Int = total() - neto()
}
