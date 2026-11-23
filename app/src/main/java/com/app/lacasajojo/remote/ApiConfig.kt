package com.app.lacasajojo.remote

import java.util.concurrent.TimeUnit
import com.app.lacasajojo.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// ---------------------------------------------------------------------
// Dónde están los microservicios.
//  - Emulador de Android Studio: 10.0.2.2 apunta al PC donde corre Spring Boot.
//  - Teléfono físico: compilar con -PjojoBackendHost=IP_DEL_PC (misma WiFi).
// ---------------------------------------------------------------------
object ApiConfig {
    // Define -PjojoBackendHost=IP_DEL_PC al compilar para un celular físico.
    val HOST: String = BuildConfig.BACKEND_HOST
    const val PUERTO_USUARIOS = 8081
    const val PUERTO_MENU = 8082
    const val PUERTO_PEDIDOS = 8083
    const val URL_CLIMA = "https://api.open-meteo.com/"

    // Ubicación del clima conservada del proyecto del equipo (Santiago)
    const val LATITUD = -33.45
    const val LONGITUD = -70.66

    private val cliente: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(4, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .writeTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(url: String): Retrofit = Retrofit.Builder()
        .baseUrl(url)
        .client(cliente)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun local(puerto: Int) = "http://$HOST:$puerto/"

    fun crearBackend(): BackendApi = BackendApi(
        usuarios = retrofit(local(PUERTO_USUARIOS)).create(UsuarioApi::class.java),
        menu = retrofit(local(PUERTO_MENU)).create(MenuApi::class.java),
        pedidos = retrofit(local(PUERTO_PEDIDOS)).create(PedidoApi::class.java)
    )

    fun crearClimaApi(): ClimaApi = retrofit(URL_CLIMA).create(ClimaApi::class.java)
}
