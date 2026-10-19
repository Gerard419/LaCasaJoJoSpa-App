package com.app.lacasajojo

import com.app.lacasajojo.model.EstadoPedido
import com.app.lacasajojo.model.ItemPedido
import com.app.lacasajojo.model.Pedido
import com.app.lacasajojo.utils.clp
import com.app.lacasajojo.utils.esEmailValido
import com.app.lacasajojo.utils.esPasswordValida
import com.app.lacasajojo.utils.validarAgregados
import com.app.lacasajojo.utils.validarConfirmacion
import com.app.lacasajojo.utils.validarEmail
import com.app.lacasajojo.utils.validarEnsalada
import com.app.lacasajojo.utils.validarMonto
import com.app.lacasajojo.utils.validarNombre
import com.app.lacasajojo.utils.validarNombrePlato
import com.app.lacasajojo.utils.validarPassword
import com.app.lacasajojo.utils.validarTelefono
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LogicaNegocioTest {

    private fun pedidoEjemplo() = Pedido(
        numero = 1,
        emailCliente = "cliente@correo.cl",
        nombreCliente = "Cliente",
        fecha = "24-09-2026 13:05",
        items = listOf(
            ItemPedido("Pollo Asado", 4800, listOf("Arroz integral", "Puré (natural)"), "Mix de ensaladas", 1),
            ItemPedido("Cerdo mongoliano", 4600, listOf("Fideos al pesto"), "Mix de ensaladas", 1)
        ),
        observaciones = "",
        estado = EstadoPedido.RECIBIDO
    )

    @Test
    fun totalSumaPrecioPorCantidad() {
        assertEquals(9400, pedidoEjemplo().total())
    }

    @Test
    fun netoMasIvaEsIgualAlTotal() {
        val pedido = pedidoEjemplo()
        assertEquals(7899, pedido.neto())
        assertEquals(1501, pedido.iva())
        assertEquals(pedido.total(), pedido.neto() + pedido.iva())
    }

    @Test
    fun formatoPesos() {
        assertEquals("\$4.600", 4600.clp())
        assertEquals("\$14.200", 14200.clp())
        assertEquals("\$999", 999.clp())
    }

    @Test
    fun validacionDePassword() {
        assertTrue(esPasswordValida("Abc12345"))
        assertFalse(esPasswordValida("abcdefgh"))
        assertFalse(esPasswordValida("1234567"))
    }

    @Test
    fun validacionDeCorreo() {
        assertTrue(esEmailValido("dueno@lacasajojo.cl"))
        assertFalse(esEmailValido("correo-sin-arroba"))
    }

    // ---- Validadores con mensaje (null = válido)

    @Test
    fun validarNombreDevuelveMensaje() {
        assertNull(validarNombre("Ana Pérez"))
        assertEquals("El nombre es obligatorio", validarNombre("  "))
        assertNotNull(validarNombre("A"))
        assertNotNull(validarNombre("Ana2"))
        assertNotNull(validarNombre("a".repeat(41)))
    }

    @Test
    fun validarEmailYPassword() {
        assertNull(validarEmail("cliente@correo.cl"))
        assertEquals("El correo es obligatorio", validarEmail(""))
        assertEquals("El formato del correo no es válido", validarEmail("sin-arroba"))
        assertNull(validarPassword("Abc12345"))
        assertNotNull(validarPassword("abcdefgh"))
        assertNull(validarConfirmacion("Abc12345", "Abc12345"))
        assertEquals("Las contraseñas no coinciden", validarConfirmacion("Abc12345", "Abc12346"))
    }

    @Test
    fun validarTelefonoChileno() {
        assertNull(validarTelefono("912345678"))
        assertNotNull(validarTelefono("12345678"))   // 8 dígitos
        assertNotNull(validarTelefono("812345678"))  // no empieza con 9
        assertNotNull(validarTelefono("9123abc78"))  // letras
        assertNotNull(validarTelefono(""))
    }

    @Test
    fun validarPrecioDelPlato() {
        assertNull(validarMonto("4800"))
        assertNotNull(validarMonto(""))
        assertNotNull(validarMonto("abc"))
        assertNotNull(validarMonto("0"))
        assertNotNull(validarMonto("-100"))
        assertNotNull(validarMonto("999999"))
    }

    @Test
    fun validarNombreDelPlato() {
        assertNull(validarNombrePlato("Pollo Asado"))
        assertNotNull(validarNombrePlato("ab"))
        assertNotNull(validarNombrePlato("p".repeat(41)))
    }

    @Test
    fun reglaDeAgregadosYEnsalada() {
        assertNotNull(validarAgregados(0))          // mínimo 1
        assertNull(validarAgregados(1))
        assertNull(validarAgregados(2))
        assertNotNull(validarAgregados(3))          // máximo 2
        assertNotNull(validarEnsalada(""))          // la ensalada es obligatoria
        assertNull(validarEnsalada("Mix de ensaladas"))
    }
}
