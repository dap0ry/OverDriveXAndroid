package com.example.overdrivexandroid.feature_auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.overdrivexandroid.feature_auth.data.AuthRepository
import com.example.overdrivexandroid.feature_auth.domain.LoginRequest
import com.example.overdrivexandroid.feature_auth.domain.RegisterRequest
import com.example.overdrivexandroid.feature_auth.domain.VerifyRegistrationRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI de autenticación como una sola data class inmutable.
// Siguiendo el patrón Unidirectional Data Flow (UDF): la UI observa el estado
// y el ViewModel lo actualiza con `copy()`, nunca mutar directamente.
data class AuthState(
    val isLoading: Boolean = false,
    val successMessage: String? = null,
    val errorMessage: String? = null,
    val token: String? = null,              // token JWT recibido tras el login
    val showVerificationStep: Boolean = false  // controla si mostramos el paso 2 del registro
)

// AuthViewModel gestiona la lógica de login y registro para las pantallas de autenticación.
// Usa StateFlow para exponer el estado a la UI de forma reactiva (Compose lo observa con collectAsState).
class AuthViewModel : ViewModel() {

    // El repositorio es quien habla con la API; el ViewModel solo coordina
    private val repository = AuthRepository()

    // Estado interno mutable (solo modificable desde el ViewModel)
    private val _state = MutableStateFlow(AuthState())

    // Estado público solo de lectura para la UI
    val state: StateFlow<AuthState> = _state.asStateFlow()

    // Inicia el proceso de login: envía email y contraseña al servidor
    fun login(request: LoginRequest) {
        // viewModelScope cancela la corrutina automáticamente si el ViewModel se destruye
        viewModelScope.launch {
            // Indicamos que estamos cargando y limpiamos mensajes anteriores
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = repository.login(request)

            result.onSuccess { authResp ->
                // Login correcto: guardamos el token en el estado para que la UI navegue al home
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "¡Bienvenido ${authResp.username}!",
                        token = authResp.access_token
                    )
                }
            }.onFailure { error ->
                // Login fallido: mostramos el error al usuario
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error desconocido al iniciar sesión"
                    )
                }
            }
        }
    }

    // Paso 1 del registro: solicita al servidor que envíe un código de verificación al email
    fun requestVerification(request: RegisterRequest) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = repository.requestVerification(request)

            result.onSuccess { authResp ->
                // El servidor envió el email: mostramos el paso 2 (introducir el código)
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = authResp.msg ?: "Código enviado a tu correo.",
                        showVerificationStep = true
                    )
                }
            }.onFailure { error ->
                // El servidor rechazó el registro (email duplicado, datos inválidos...)
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Error al solicitar el código de verificación"
                    )
                }
            }
        }
    }

    // Paso 2 del registro: envía el código de 6 dígitos al servidor para completar la cuenta
    fun verifyRegistration(request: VerifyRegistrationRequest) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }

            val result = repository.verifyRegistration(request)

            result.onSuccess { authResp ->
                // Cuenta verificada con éxito: volvemos al paso 1 (pantalla de login)
                _state.update {
                    it.copy(
                        isLoading = false,
                        successMessage = authResp.msg ?: "¡Cuenta verificada! Puedes iniciar sesión.",
                        showVerificationStep = false
                    )
                }
            }.onFailure { error ->
                // Código incorrecto o expirado
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Código de verificación inválido"
                    )
                }
            }
        }
    }

    // Limpia los mensajes de error y éxito (se llama tras mostrarlos con un Snackbar o similar)
    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
