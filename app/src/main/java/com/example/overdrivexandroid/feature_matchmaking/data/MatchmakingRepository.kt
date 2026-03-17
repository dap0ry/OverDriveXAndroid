package com.example.overdrivexandroid.feature_matchmaking.data

import com.example.overdrivexandroid.core.network.RetrofitClient
import com.example.overdrivexandroid.feature_matchmaking.domain.JoinRequest
import com.example.overdrivexandroid.feature_matchmaking.domain.MatchStatusResponse
import com.example.overdrivexandroid.feature_matchmaking.domain.ReportRequest
import com.example.overdrivexandroid.feature_matchmaking.domain.ReportResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// MatchmakingRepository se comunica con las rutas /matchmaking de la API.
class MatchmakingRepository {
    private val api = RetrofitClient.instance.create(MatchmakingApiService::class.java)

    // Úne al usuario a la cola de búsqueda para los juegos seleccionados
    suspend fun joinQueue(games: List<String>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.joinQueue(JoinRequest(games))
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Error al unirse a la cola: HTTP ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Cancela la búsqueda actual y saca al usuario de la cola
    suspend fun cancelSearch(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.cancelSearch()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Pregunta al servidor en qué estado estamos (en la cola, partida encontrada, o nada)
    suspend fun getStatus(): Result<MatchStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStatus()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al obtener estado: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Reporta si ganamos (WIN) o perdimos (LOSS) la partida
    suspend fun reportResult(matchId: String, result: String): Result<ReportResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.reportResult(ReportRequest(matchId, result))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al reportar resultado: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
