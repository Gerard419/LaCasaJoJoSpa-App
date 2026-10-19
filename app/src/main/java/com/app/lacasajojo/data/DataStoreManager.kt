package com.app.lacasajojo.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.app.lacasajojo.model.*
import com.app.lacasajojo.model.Extra
import com.app.lacasajojo.model.Pedido
import com.app.lacasajojo.model.Plato
import com.app.lacasajojo.model.Usuario
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.text.get

val Context.dataStore by preferencesDataStore(name = "lacasajojo_prefs")

class DataStoreManager(private val context: Context) {
    private val gson = Gson()

    private val USUARIOS_KEY = stringPreferencesKey("usuarios")
    private val PLATOS_KEY = stringPreferencesKey("platos")
    private val EXTRAS_KEY = stringPreferencesKey("extras")
    private val PEDIDOS_KEY = stringPreferencesKey("pedidos")

    private suspend fun guardar(clave: Preferences.Key<String>, datos: List<Any>) {
        val json = gson.toJson(datos)
        context.dataStore.edit { prefs -> prefs[clave] = json }
    }

    // ---- Usuarios
    suspend fun saveUsuarios(lista: List<Usuario>) = guardar(USUARIOS_KEY, lista)

    fun getUsuarios(): Flow<List<Usuario>> {
        return context.dataStore.data.map { prefs ->
            val tipo = object : TypeToken<List<Usuario>>() {}.type
            gson.fromJson<List<Usuario>>(prefs[USUARIOS_KEY] ?: "[]", tipo)
        }
    }

    // ---- Platos
    suspend fun savePlatos(lista: List<Plato>) = guardar(PLATOS_KEY, lista)

    fun getPlatos(): Flow<List<Plato>> {
        return context.dataStore.data.map { prefs ->
            val tipo = object : TypeToken<List<Plato>>() {}.type
            gson.fromJson<List<Plato>>(prefs[PLATOS_KEY] ?: "[]", tipo)
        }
    }

    // ---- Agregados y ensaladas
    suspend fun saveExtras(lista: List<Extra>) = guardar(EXTRAS_KEY, lista)

    fun getExtras(): Flow<List<Extra>> {
        return context.dataStore.data.map { prefs ->
            val tipo = object : TypeToken<List<Extra>>() {}.type
            gson.fromJson<List<Extra>>(prefs[EXTRAS_KEY] ?: "[]", tipo)
        }
    }

    // ---- Pedidos
    suspend fun savePedidos(lista: List<Pedido>) = guardar(PEDIDOS_KEY, lista)

    fun getPedidos(): Flow<List<Pedido>> {
        return context.dataStore.data.map { prefs ->
            val tipo = object : TypeToken<List<Pedido>>() {}.type
            gson.fromJson<List<Pedido>>(prefs[PEDIDOS_KEY] ?: "[]", tipo)
        }
    }
}
