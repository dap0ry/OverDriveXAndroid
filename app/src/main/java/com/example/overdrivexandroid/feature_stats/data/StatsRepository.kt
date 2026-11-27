package com.example.overdrivexandroid.feature_stats.data

import com.example.overdrivexandroid.core.network.RetrofitClient
import com.example.overdrivexandroid.feature_stats.domain.StatsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// StatsRepository se comunica con las rutas /stats de la API
class StatsRepository {
    private val api = RetrofitClient.instance.create(StatsApiService::class.java)

    // Obtiene las estadísticas detalladas (globales y por juego) de un usuario concreto
    suspend fun fetchStats(userId: String): Result<StatsResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.getStats(userId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Stats fetch failed: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
