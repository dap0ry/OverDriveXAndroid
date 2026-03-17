package com.example.overdrivexandroid.feature_stats.data

import com.example.overdrivexandroid.feature_stats.domain.StatsResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface StatsApiService {
    @GET("stats/{user_id}")
    suspend fun getStats(@Path("user_id") userId: String): Response<StatsResponse>
}
