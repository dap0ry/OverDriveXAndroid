package com.example.overdrivexandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.overdrivexandroid.core.session.SessionManager
import com.example.overdrivexandroid.feature_auth.ui.LoginScreen
import com.example.overdrivexandroid.feature_auth.ui.RegisterScreen
import com.example.overdrivexandroid.ui.theme.OverDriveXAndroidTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Instala la Splash Screen ANTES que cualquier otra cosa
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            OverDriveXAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    val navController = rememberNavController()
                    val token by SessionManager.getTokenFlow(context).collectAsState(initial = null)

                    // Auto login logic
                    LaunchedEffect(token) {
                        if (token != null) {
                            if (!SessionManager.isTokenExpired(token!!)) {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            } else {
                                SessionManager.clearSession(context)
                            }
                        }
                    }

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                onNavigateToRegister = { navController.navigate("register") },
                                onLoginSuccess = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("register") {
                            RegisterScreen(
                                onNavigateToLogin = { navController.navigate("login") { popUpTo("login") { inclusive = true } } }
                            )
                        }
                        composable("main") {
                            val userId = token?.let { if (!SessionManager.isTokenExpired(it)) SessionManager.extractUserIdFromToken(it) else null }
                            val scope = rememberCoroutineScope()

                            if (userId != null) {
                                MainAppScreen(userId = userId, onLogout = {
                                    scope.launch {
                                        kotlinx.coroutines.delay(100) // Small delay to prevent race condition 
                                        SessionManager.clearSession(context)
                                    }
                                    navController.navigate("login") { popUpTo(0) }
                                })
                            } else {
                                // Fallback
                                LaunchedEffect(Unit) {
                                    navController.navigate("login") { popUpTo(0) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}