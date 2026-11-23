package com.app.lacasajojo

import com.app.lacasajojo.utils.*
import org.junit.Assert.*
import org.junit.Test

/** Cobertura adicional de ramas de las reglas de negocio de la app. */
class ValidacionesCoberturaTest {
    @Test fun emailConEspaciosYCaracteresValidos() {
        assertTrue(esEmailValido("  cliente+1@correo.cl  "))
        assertFalse(esEmailValido("cliente@"))
        assertFalse(esEmailValido("cliente sin correo"))
    }

    @Test fun passwordValidaYCasosLimitrofes() {
        assertTrue(esPasswordValida("abcde123"))
        assertFalse(esPasswordValida("abc123"))
        assertFalse(esPasswordValida("abcdefgh"))
        assertFalse(esPasswordValida("12345678"))
        assertNotNull(validarPassword(""))
        assertNull(validarPassword("abcde123"))
        assertNull(validarPasswordLogin("incorrecta"))
        assertNotNull(validarPasswordLogin("  "))
    }

    @Test fun confirmacionDePassword() {
        assertNotNull(validarConfirmacion("abcde123", ""))
        assertNotNull(validarConfirmacion("abcde123", "abcde124"))
        assertNull(validarConfirmacion("abcde123", "abcde123"))
    }

    @Test fun nombreDebeTenerLongitudYCeroNumeros() {
        assertNotNull(validarNombre(""))
        assertNotNull(validarNombre("a"))
        assertNotNull(validarNombre("x".repeat(MAX_NOMBRE + 1)))
        assertNotNull(validarNombre("Cliente9"))
        assertNull(validarNombre("María José"))
    }

    @Test fun validacionEmailDistingueLargoDeFormato() {
        assertNotNull(validarEmail(""))
        assertNotNull(validarEmail("x".repeat(MAX_EMAIL + 1)))
        assertNotNull(validarEmail("sin-arroba"))
        assertNull(validarEmail("persona@duoc.cl"))
    }

    @Test fun telefonoChileno() {
        assertNotNull(validarTelefono(""))
        assertNotNull(validarTelefono("91234a678"))
        assertNotNull(validarTelefono("812345678"))
        assertNotNull(validarTelefono("91234567"))
        assertNull(validarTelefono("912345678"))
    }

    @Test fun precioConTodosSusMensajes() {
        assertEquals("El precio es obligatorio", validarMonto(""))
        assertNotNull(validarMonto("abc"))
        assertNotNull(validarMonto("0"))
        assertNotNull(validarMonto("100001"))
        assertNull(validarMonto(" 4800 "))
    }

    @Test fun nombreDelPlato() {
        assertNotNull(validarNombrePlato(""))
        assertNotNull(validarNombrePlato("ab"))
        assertNotNull(validarNombrePlato("x".repeat(MAX_PLATO + 1)))
        assertNull(validarNombrePlato("Pastel de choclo"))
    }

    @Test fun reglasDelMenu() {
        assertNotNull(validarAgregados(0))
        assertNull(validarAgregados(1))
        assertNull(validarAgregados(2))
        assertNotNull(validarAgregados(3))
        assertNotNull(validarEnsalada(" "))
        assertNull(validarEnsalada("Ensalada chilena"))
    }

    @Test fun formatosDeBoletaYDinero() {
        assertEquals("000001", 1.formatoBoleta())
        assertEquals("123456", 123456.formatoBoleta())
        assertEquals("\$10.000", 10000.clp())
    }

    @Test fun hashEsDeterministaYNoGuardaTextoPlano() {
        val hash = hashPassword("Password123")
        assertEquals(64, hash.length)
        assertEquals(hash, hashPassword("Password123"))
        assertNotEquals(hash, hashPassword("Password124"))
    }
}
