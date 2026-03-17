package com.example.overdrivexandroid.feature_auth.data

import com.example.overdrivexandroid.core.network.RetrofitClient
import com.example.overdrivexandroid.feature_auth.domain.AuthResponse
import com.example.overdrivexandroid.feature_auth.domain.LoginRequest
import com.example.overdrivexandroid.feature_auth.domain.RegisterRequest
import com.example.overdrivexandroid.feature_auth.domain.VerifyRegistrationRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// AuthRepository actúa como intermediario entre el ViewModel y la API.
// El ViewModel no sabe nada de Retrofit ni de HTTP: solo llama a métodos de esta clase
// y recibe un Result<T> que puede ser éxito o error.
// Esto sigue el patrón Repository de la arquitectura limpia (Clean Architecture).
class AuthRepository {

    // Creamos el servicio de la API a partir de la instancia de Retrofit
    private val api = RetrofitClient.instance.create(AuthApiService::class.java)

    // Envía las credenciales al servidor para iniciar sesión.
    // Se ejecuta en Dispatchers.IO para no bloquear el hilo principal de la UI.
    suspend fun login(request: LoginRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(request)
            if (response.isSuccessful) {
                Result.success(response.body() ?: AuthResponse(msg = "Respuesta vacía del servidor"))
            } else {
                // El servidor devolvió un error (401 credenciales incorrectas, 400 datos inválidos...)
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            // Error de red: sin conexión, timeout, DNS...
            Result.failure(e)
        }
    }

    // Envía los datos de registro al servidor, que a su vez manda un código al email del usuario.
    // Es el primer paso del registro de dos fases.
    suspend fun requestVerification(request: RegisterRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.requestVerification(request)
            if (response.isSuccessful) {
                Result.success(response.body() ?: AuthResponse(msg = "Email de verificación enviado"))
            } else {
                // El servidor puede fallar si el email o nombre de usuario ya existen
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Verifica el código de 6 dígitos que el usuario recibió por email.
    // Si es correcto, la cuenta queda activada y el usuario puede iniciar sesión.
    suspend fun verifyRegistration(request: VerifyRegistrationRequest): Result<AuthResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.verifyRegistration(request)
            if (response.isSuccessful) {
                Result.success(response.body() ?: AuthResponse(msg = "Verificación completada"))
            } else {
                Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
