package com.example.overdrivexandroid.feature_home.data

import com.example.overdrivexandroid.feature_home.domain.DashboardResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface DashboardApiService {
    @GET("dashboard/{user_id}")
    suspend fun getDashboard(@Path("user_id") userId: String): Response<DashboardResponse>
}
