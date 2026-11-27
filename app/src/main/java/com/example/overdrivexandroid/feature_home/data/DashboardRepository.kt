package com.example.overdrivexandroid.feature_home.data

import android.content.Context
import com.example.overdrivexandroid.core.db.CachedDashboard
import com.example.overdrivexandroid.core.db.DashboardDao
import com.example.overdrivexandroid.core.network.NetworkMonitor
import com.example.overdrivexandroid.core.network.RetrofitClient
import com.example.overdrivexandroid.feature_home.domain.DashboardResponse
import com.example.overdrivexandroid.feature_home.domain.MatchHistory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Resultado compuesto que devuelve tanto los datos del Dashboard como un flag
// indicando si vienen de Room (caché offline) o de la API real.
data class DashboardResult(
    val data: DashboardResponse,
    val isOffline: Boolean
)

// DashboardRepository maneja la lógica de obtener los datos principales del usuario.
// Implementa el patrón "Caché Offline-First": si no hay red, intenta leer de Room.
// Si hay red, lee de la API y guarda una copia reciente en Room por si luego se pierde.
class DashboardRepository(private val dao: DashboardDao) {
    private val api = RetrofitClient.instance.create(DashboardApiService::class.java)
    private val gson = Gson()

    // Realiza la petición combinando comprobación de red y acceso a BD en hilo secundario (IO)
    suspend fun fetchDashboard(userId: String, context: Context): Result<DashboardResult> =
        withContext(Dispatchers.IO) {
            if (!NetworkMonitor.isOnline(context)) {
                // Modo Offline: no hay internet, comprobamos caché
                val cached = dao.get()
                if (cached != null) {
                    // Mapeamos de entidad de base de datos a modelo de dominio
                    Result.success(DashboardResult(cached.toDomain(), isOffline = true))
                } else {
                    Result.failure(Exception("Sin conexión y sin datos guardados"))
                }
            } else {
                // Modo Online: hay conexión, pedimos a la API
                try {
                    val response = api.getDashboard(userId)
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!
                        // Guardamos esta última respuesta buena en caché local (Room)
                        dao.save(body.toCached(gson))
                        Result.success(DashboardResult(body, isOffline = false))
                    } else {
                        Result.failure(Exception("Fallo al obtener dashboard: HTTP ${response.code()}"))
                    }
                } catch (e: Exception) {
                    // Fallo de red (por ejemplo, el servidor está caído pero sí hay WiFi)
                    // Hacemos un intento final de cargar la información vieja de la caché
                    val cached = dao.get()
                    if (cached != null) {
                        Result.success(DashboardResult(cached.toDomain(), isOffline = true))
                    } else {
                        Result.failure(e)
                    }
                }
            }
        }

    // ── Funciones de Mapeo (Helpers) ──────────────────────────────────────────────────

    // Convierte el objeto de respuesta del API a una entidad plana que Room puede guardar en SQLite
    private fun DashboardResponse.toCached(gson: Gson) = CachedDashboard(
        globalRank       = stats.global_rank,
        level            = level,
        globalElo        = stats.global_elo,
        totalWins        = stats.total_wins,
        totalTournaments = total_tournaments,
        // Serializamos las listas o arrays anidados como JSON String para la BD
        lastMatchesJson  = gson.toJson(last_matches)
    )

    // Recupera la entidad de la BD y extrae la string JSON a la lista de partidas real
    private fun CachedDashboard.toDomain(): DashboardResponse {
        val matchType = object : TypeToken<List<MatchHistory>>() {}.type
        val matches: List<MatchHistory> = gson.fromJson(lastMatchesJson, matchType) ?: emptyList()
        
        return DashboardResponse(
            username           = "",
            level              = level,
            xp                 = 0,
            total_tournaments  = totalTournaments,
            // Reconstruimos a mano la estructura anidada ya que perdimos parte online (solo cacheamos lo mínimo)
            stats              = com.example.overdrivexandroid.feature_home.domain.GlobalStats(
                global_elo   = globalElo,
                global_rank  = globalRank,
                total_wins   = totalWins,
                total_losses = 0,
                total_matches = 0
            ),
            last_matches = matches,
            friends      = com.example.overdrivexandroid.feature_home.domain.FriendStats(0, 0, 0)
        )
    }
}
