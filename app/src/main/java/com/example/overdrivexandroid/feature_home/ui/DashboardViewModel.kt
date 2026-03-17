package com.example.overdrivexandroid.feature_home.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.overdrivexandroid.core.db.AppDatabase
import com.example.overdrivexandroid.core.session.SessionManager
import com.example.overdrivexandroid.feature_home.data.DashboardRepository
import com.example.overdrivexandroid.feature_home.domain.DashboardResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la UI del Dashboard. Mantiene si está cargando, los datos descargados, 
// posibles mensajes de error, y si estamos mostrando datos cacheados offline.
data class DashboardState(
    val isLoading: Boolean = true,
    val data: DashboardResponse? = null,
    val errorMessage: String? = null,
    val isOffline: Boolean = false
)

// ViewModel del Dashboard. Usamos AndroidViewModel en lugar de ViewModel regular 
// porque necesitamos el contexto de la aplicación (Application) para acceder a la BD local (Room).
class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    // Inicializamos el acceso a la base de datos local y al repositorio del Dashboard
    private val dao = AppDatabase.getInstance(application).dashboardDao()
    private val repository = DashboardRepository(dao)

    // Estado inmutable expuesto a la UI y su versión mutable interna
    private val _state = MutableStateFlow(DashboardState())
    val state: StateFlow<DashboardState> = _state.asStateFlow()

    // Carga los datos del dashboard desde la API o, si no hay internet, desde Room
    fun loadDashboard(userId: String) {
        viewModelScope.launch {
            // Empieza mostrando el indicador de carga
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.fetchDashboard(userId, getApplication())

            result.onSuccess { dashResult ->
                // Guardamos el avatar en SessionManager solo si lo hemos traído fresco de internet
                if (!dashResult.isOffline && !dashResult.data.avatar_url.isNullOrEmpty()) {
                    SessionManager.saveAvatarUrl(getApplication(), dashResult.data.avatar_url)
                }
                // Actualizamos la UI con los nuevos datos
                _state.update {
                    it.copy(
                        isLoading = false,
                        data = dashResult.data,
                        isOffline = dashResult.isOffline
                    )
                }
            }.onFailure { error ->
                // Si la red y la caché fallan, mostramos el error
                _state.update { it.copy(isLoading = false, errorMessage = error.message) }
            }
        }
    }
}
