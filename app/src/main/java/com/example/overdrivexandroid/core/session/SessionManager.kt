package com.example.overdrivexandroid.core.session

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONObject

// DataStore es la alternativa moderna a SharedPreferences en Android.
// Lo usamos para guardar el token de sesión y las URLs de imagen de forma persistente
// (sobreviven a cerrar y reabrir la app).
private val Context.dataStore by preferencesDataStore(name = "user_prefs")

// SessionManager gestiona toda la información de la sesión del usuario.
// Combina una caché en memoria (para acceso rápido desde el interceptor de red)
// con persistencia en DataStore (para que la sesión sobreviva reinicios).
object SessionManager {
    // Claves para los valores guardados en DataStore (como strings de preferencias)
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val AVATAR_KEY = stringPreferencesKey("avatar_url")
    private val FULL_CHAR_KEY = stringPreferencesKey("full_char_url")

    // Caché en memoria para que el AuthInterceptor pueda leer el token de forma síncrona.
    // OkHttp no puede usar corrutinas, así que necesita acceso directo sin suspend.
    var currentToken: String? = null
    var currentAvatarUrl: String? = null
    var currentFullCharUrl: String? = null

    // Guarda el token JWT en DataStore y actualiza la caché en memoria
    suspend fun saveToken(context: Context, token: String) {
        context.dataStore.edit { prefs ->
            prefs[TOKEN_KEY] = token
            currentToken = token
        }
    }

    // Guarda la URL del avatar (y opcionalmente la del personaje completo) del usuario
    suspend fun saveAvatarUrl(context: Context, avatarUrl: String, fullCharUrl: String? = null) {
        currentAvatarUrl = avatarUrl
        if (fullCharUrl != null) currentFullCharUrl = fullCharUrl
        context.dataStore.edit { prefs ->
            prefs[AVATAR_KEY] = avatarUrl
            if (fullCharUrl != null) {
                prefs[FULL_CHAR_KEY] = fullCharUrl
            }
        }
    }

    // Recupera la URL del avatar desde DataStore (lectura asíncrona)
    suspend fun getAvatarUrl(context: Context): String? {
        return context.dataStore.data.first()[AVATAR_KEY]
    }

    // Recupera la URL del personaje completo (imagen de cuerpo entero) desde DataStore
    suspend fun getFullCharUrl(context: Context): String? {
        return context.dataStore.data.first()[FULL_CHAR_KEY]
    }

    // Borra todos los datos de sesión al cerrar sesión.
    // Se limpia tanto el DataStore (persistente) como la caché en memoria.
    suspend fun clearSession(context: Context) {
        context.dataStore.edit { prefs ->
            prefs.remove(TOKEN_KEY)
            prefs.remove(AVATAR_KEY)
            prefs.remove(FULL_CHAR_KEY)
            currentToken = null
            currentAvatarUrl = null
            currentFullCharUrl = null
        }
    }

    // Devuelve un Flow del token para que la UI pueda reaccionar si el token cambia.
    // Útil para detectar si el usuario ha cerrado sesión desde otro lugar.
    fun getTokenFlow(context: Context): Flow<String?> {
        return context.dataStore.data.map { prefs ->
            val token = prefs[TOKEN_KEY]
            currentToken = token  // sincronizamos la caché en memoria al leer
            token
        }
    }

    /**
     * Extrae el user_id (identidad) del payload del token JWT.
     * El formato del JWT es: cabecera.payload.firma (separados por puntos).
     * El payload está en Base64 y contiene los "claims" como sub, exp, etc.
     */
    fun extractUserIdFromToken(token: String): String? {
        try {
            val parts = token.split(".")
            if (parts.size == 3) {
                // Decodificamos el payload de Base64 URL-safe a String JSON
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val json = JSONObject(payload)
                // En flask_jwt_extended, el campo "sub" (subject) contiene el ID del usuario
                return json.optString("sub", null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Comprueba si el token JWT ha expirado leyendo el campo "exp" del payload.
     * "exp" contiene la fecha de expiración en segundos desde el epoch Unix.
     */
    fun isTokenExpired(token: String): Boolean {
        try {
            val parts = token.split(".")
            if (parts.size == 3) {
                val payload = String(Base64.decode(parts[1], Base64.URL_SAFE))
                val json = JSONObject(payload)
                if (json.has("exp")) {
                    val exp = json.getLong("exp")
                    val currentTimeSeconds = System.currentTimeMillis() / 1000
                    // Si el tiempo actual supera la expiración, el token ya no vale
                    return currentTimeSeconds >= exp
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }
}
