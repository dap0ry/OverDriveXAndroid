package com.example.overdrivexandroid.feature_auth.data

import com.example.overdrivexandroid.feature_auth.domain.AuthResponse
import com.example.overdrivexandroid.feature_auth.domain.LoginRequest
import com.example.overdrivexandroid.feature_auth.domain.RegisterRequest
import com.example.overdrivexandroid.feature_auth.domain.VerifyRegistrationRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/request-verification")
    suspend fun requestVerification(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/verify-registration")
    suspend fun verifyRegistration(@Body request: VerifyRegistrationRequest): Response<AuthResponse>
}
