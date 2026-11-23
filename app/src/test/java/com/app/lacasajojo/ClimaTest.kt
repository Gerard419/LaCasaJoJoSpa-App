package com.app.lacasajojo

import com.app.lacasajojo.remote.ClimaActualDto
import com.app.lacasajojo.remote.ClimaApi
import com.app.lacasajojo.remote.ClimaResponse
import com.app.lacasajojo.repository.ClimaRepository
import com.app.lacasajojo.utils.descripcionClima
import com.app.lacasajojo.utils.sugerenciaPorClima
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ClimaTest {

    private val api = mockk<ClimaApi>()
    private val repo = ClimaRepository(api)

    @Test
    fun convierteLaRespuestaDeLaApi() = runTest {
        coEvery { api.clima(any(), any(), true) } returns ClimaResponse(ClimaActualDto(21.5, 12.0, 3))
        val clima = repo.obtenerClima()
        assertNotNull(clima)
        assertEquals(21.5, clima!!.temperatura, 0.001)
        assertEquals(3, clima.codigo)
    }

    @Test
    fun sinDatosActualesDevuelveNull() = runTest {
        coEvery { api.clima(any(), any(), any()) } returns ClimaResponse(null)
        assertNull(repo.obtenerClima())
    }

    @Test
    fun siLaApiFallaDevuelveNullYLaAppSigue() = runTest {
        coEvery { api.clima(any(), any(), any()) } throws IOException("sin red")
        assertNull(repo.obtenerClima(-33.0, -70.0))
    }

    @Test
    fun descripcionesDeClima() {
        assertEquals("Despejado", descripcionClima(0))
        assertEquals("Parcialmente nublado", descripcionClima(2))
        assertEquals("Neblina", descripcionClima(45))
        assertEquals("Llovizna", descripcionClima(53))
        assertEquals("Lluvia", descripcionClima(63))
        assertEquals("Nieve", descripcionClima(73))
        assertEquals("Chubascos", descripcionClima(81))
        assertEquals("Tormenta", descripcionClima(95))
        assertEquals("Sin información", descripcionClima(500))
    }

    @Test
    fun sugerenciasSegunTemperaturaYLluvia() {
        assertTrue(sugerenciaPorClima(18.0, 63).contains("lluvia"))
        assertTrue(sugerenciaPorClima(8.0, 0).contains("frío"))
        assertTrue(sugerenciaPorClima(30.0, 0).contains("caluroso"))
        assertTrue(sugerenciaPorClima(18.0, 0).contains("JoJo"))
    }
}
