package com.app.lacasajojo.remote

import com.app.lacasajojo.model.EstadoPedido
import com.app.lacasajojo.model.Extra
import com.app.lacasajojo.model.Pedido
import com.app.lacasajojo.model.Plato
import com.app.lacasajojo.model.Usuario
import com.google.gson.annotations.SerializedName
import retrofit2.Response
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

const val HEADER_ADMIN = "X-Admin-Email"

data class LoginRequest(val email: String, val passwordHash: String)

// ---------------------------------------------------------------------
// Microservicios propios (Spring Boot)
// ---------------------------------------------------------------------

// usuarios-service (puerto 8081)
interface UsuarioApi {
    @GET("api/usuarios")
    suspend fun listar(@Header(HEADER_ADMIN) adminEmail: String?): List<Usuario>

    @POST("api/usuarios/login")
    suspend fun login(@Body datos: LoginRequest): Response<Usuario>

    @POST("api/usuarios")
    suspend fun crear(@Body usuario: Usuario, @Header(HEADER_ADMIN) adminEmail: String?): Response<Usuario>

    @PUT("api/usuarios/estado")
    suspend fun cambiarEstado(
        @Query("email") email: String,
        @Query("activo") activo: Boolean,
        @Header(HEADER_ADMIN) adminEmail: String?
    ): Response<Usuario>
}

// menu-service (puerto 8082)
data class FotoSubidaResponse(val ruta: String)

interface MenuApi {
    @Multipart
    @POST("api/fotos")
    suspend fun subirFoto(@Part archivo: MultipartBody.Part): Response<FotoSubidaResponse>

    @GET("api/platos")
    suspend fun listarPlatos(): List<Plato>

    @POST("api/platos")
    suspend fun crearPlato(@Body plato: Plato): Response<Plato>

    @PUT("api/platos/{id}")
    suspend fun actualizarPlato(@Path("id") id: Int, @Body plato: Plato): Response<Plato>

    @DELETE("api/platos/{id}")
    suspend fun eliminarPlato(@Path("id") id: Int): Response<Unit>

    @GET("api/extras")
    suspend fun listarExtras(): List<Extra>

    @POST("api/extras")
    suspend fun crearExtra(@Body extra: Extra): Response<Extra>

    @PUT("api/extras/{id}")
    suspend fun actualizarExtra(@Path("id") id: Int, @Body extra: Extra): Response<Extra>

    @DELETE("api/extras/{id}")
    suspend fun eliminarExtra(@Path("id") id: Int): Response<Unit>
}

// pedidos-service (puerto 8083)
interface PedidoApi {
    // email = null -> todos los pedidos (dueño); con email -> solo los de ese cliente
    @GET("api/pedidos")
    suspend fun listar(@Query("email") email: String?): List<Pedido>

    @POST("api/pedidos")
    suspend fun crear(@Body pedido: Pedido): Response<Pedido>

    @PUT("api/pedidos/{numero}/estado")
    suspend fun cambiarEstado(
        @Path("numero") numero: Int,
        @Query("estado") estado: EstadoPedido
    ): Response<Pedido>
}

// Agrupa los tres microservicios para inyectarlos en el repositorio
class BackendApi(
    val usuarios: UsuarioApi,
    val menu: MenuApi,
    val pedidos: PedidoApi
)

// ---------------------------------------------------------------------
// API externa (Open-Meteo, sin clave): clima actual que se muestra en el menú del cliente
// ---------------------------------------------------------------------
interface ClimaApi {
    @GET("v1/forecast")
    suspend fun clima(
        @Query("latitude") latitud: Double,
        @Query("longitude") longitud: Double,
        @Query("current_weather") actual: Boolean
    ): ClimaResponse
}

data class ClimaResponse(
    @SerializedName("current_weather") val climaActual: ClimaActualDto?
)

data class ClimaActualDto(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int
)
