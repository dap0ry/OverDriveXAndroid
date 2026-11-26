package com.example.overdrivexandroid.feature_auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.overdrivexandroid.feature_auth.domain.RegisterRequest
import com.example.overdrivexandroid.feature_auth.domain.VerifyRegistrationRequest
import kotlinx.coroutines.delay

// Pantalla de Registro con flujo en dos pasos: 1) Formulario y 2) Código de verificación por email.
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    // Estado local para los campos del formulario
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    
    // Errores de validación rápida en local (front-end)
    var localError by remember { mutableStateOf<String?>(null) }
    
    // Temporizador para el código de verificación (2 minutos)
    var timerSeconds by remember { mutableIntStateOf(120) }

    // Corrutina que gestiona la cuenta atrás cuando llegamos al paso 2
    LaunchedEffect(state.showVerificationStep) {
        if (state.showVerificationStep) {
            timerSeconds = 120
            while (timerSeconds > 0) {
                delay(1000)
                timerSeconds--
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Unirse a OverDriveX", style = MaterialTheme.typography.headlineLarge)
        
        Spacer(modifier = Modifier.height(32.dp))

        if (!state.showVerificationStep) {
            // == PASO 1: Formulario de Registro Principal ==
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Usuario") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirmar Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
        } else {
            // == PASO 2: Verificación por Email ==
            Text(text = "Introduce el código enviado a tu correo", color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { verificationCode = it },
                label = { Text("Código de 6 dígitos") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            // Formato de tiempo (ej "01:45")
            val minutes = timerSeconds / 60
            val seconds = timerSeconds % 60
            val timeString = "%02d:%02d".format(minutes, seconds)
            
            Text(
                text = if (timerSeconds > 0) timeString else "EXPIRADO",
                color = if (timerSeconds > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Mostramos o el error de UI local (frontend) o el error del servidor (backend)
        val displayError = localError ?: state.errorMessage
        if (displayError != null) {
            Text(text = displayError, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Si la verificación fue exitosa, mostramos mensaje y navegamos al Login tras 2 segundos
        if (state.successMessage != null) {
            Text(text = state.successMessage!!, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            if (!state.showVerificationStep) {
                LaunchedEffect(state.successMessage) {
                    delay(2000)
                    viewModel.clearMessages()
                    onNavigateToLogin()
                }
            }
        }

        // Botones de acción (Registrarse, Verificar, Reenviar)
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            if (!state.showVerificationStep) {
                // Botón del Paso 1
                Button(
                    onClick = {
                        // Validaciones rápidas antes de disparar la corrutina de red
                        if (password != confirmPassword) {
                            localError = "Las contraseñas no coinciden"
                        } else if (username.isBlank() || email.isBlank() || password.isBlank()) {
                            localError = "Todos los campos son obligatorios"
                        } else {
                            localError = null
                            viewModel.requestVerification(RegisterRequest(username, email, password))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrarse")
                }
            } else {
                // Botones del Paso 2
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            if (verificationCode.isBlank() || verificationCode.length != 6) {
                                localError = "Introduce un código válido de 6 dígitos"
                            } else {
                                localError = null
                                viewModel.verifyRegistration(VerifyRegistrationRequest(email, verificationCode))
                            }
                        },
                        // Si el tiempo expira, deshabilitamos el botón de verificar
                        enabled = timerSeconds > 0,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Verificar")
                    }
                    OutlinedButton(
                        onClick = {
                            localError = null
                            viewModel.requestVerification(RegisterRequest(username, email, password)) // Reenviar código
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reenviar")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("¿Ya tienes una cuenta? Inicia sesión")
        }
    }
}
