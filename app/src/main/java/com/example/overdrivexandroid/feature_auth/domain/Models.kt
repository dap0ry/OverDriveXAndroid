package com.example.overdrivexandroid.feature_auth.domain

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val msg: String? = null,
    val access_token: String? = null,
    val username: String? = null,
    val role: String? = null
)

data class VerifyRegistrationRequest(
    val email: String,
    val code: String
)
