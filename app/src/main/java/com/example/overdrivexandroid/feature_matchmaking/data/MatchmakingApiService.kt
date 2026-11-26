package com.example.overdrivexandroid.feature_matchmaking.data

import com.example.overdrivexandroid.feature_matchmaking.domain.JoinRequest
import com.example.overdrivexandroid.feature_matchmaking.domain.MatchStatusResponse
import com.example.overdrivexandroid.feature_matchmaking.domain.ReportRequest
import com.example.overdrivexandroid.feature_matchmaking.domain.ReportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface MatchmakingApiService {

    @POST("matchmaking/join")
    suspend fun joinQueue(@Body request: JoinRequest): Response<Unit>

    @POST("matchmaking/cancel")
    suspend fun cancelSearch(): Response<Unit>

    @GET("matchmaking/status")
    suspend fun getStatus(): Response<MatchStatusResponse>

    @POST("matchmaking/report")
    suspend fun reportResult(@Body request: ReportRequest): Response<ReportResponse>
}
