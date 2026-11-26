package com.example.overdrivexandroid.feature_auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.overdrivexandroid.feature_auth.domain.LoginRequest

// Pantalla principal de inicio de sesión, construida con Jetpack Compose.
// Recibe callbacks para navegar (onNavigateToRegister, onLoginSuccess) y el ViewModel.
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    // Observamos el estado del ViewModel. Cada vez que cambie, la UI se recompone automáticamente.
    val state by viewModel.state.collectAsState()

    // Estado local para los campos de texto
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val context = androidx.compose.ui.platform.LocalContext.current

    // Reacciona a cambios en el estado: si el ViewModel recibe un token,
    // significa que el login fue exitoso. Lo guardamos y avisamos a MainAppScreen para navegar.
    LaunchedEffect(state.token) {
        if (state.token != null) {
            com.example.overdrivexandroid.core.session.SessionManager.saveToken(context, state.token!!)
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "OverDriveX", style = MaterialTheme.typography.headlineLarge)
        Text(text = "Iniciar sesion ", style = MaterialTheme.typography.bodyLarge)
        
        Spacer(modifier = Modifier.height(32.dp))

        // Campo de Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo de Contraseña (oculta los caracteres escritos)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Muestra mensaje de error si el servidor rechaza las credenciales
        if (state.errorMessage != null) {
            Text(text = state.errorMessage!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Si está haciendo la petición de red, bloqueamos el botón y mostramos el spinner
        if (state.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = { viewModel.login(LoginRequest(email, password)) },
                modifier = Modifier.fillMaxWidth(),
                // El botón solo se activa si los campos no están vacíos
                enabled = email.isNotBlank() && password.isNotBlank()
            ) {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Enlace a la pantalla de registro
        TextButton(onClick = onNavigateToRegister) {
            Text("Don't have an account? Register here")
        }
    }
}
