package com.example.overdrivexandroid.feature_stats.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.overdrivexandroid.feature_stats.data.StatsRepository
import com.example.overdrivexandroid.feature_stats.domain.GameStatSummary
import com.example.overdrivexandroid.feature_stats.domain.GlobalStatsSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la pantalla de Estadísticas (Stats)
data class StatsUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val globalStats: GlobalStatsSummary? = null,    // Estadísticas totales del jugador
    val gameStats: List<GameStatSummary> = emptyList() // Estadísticas desglosadas por cada juego
)

// StatsViewModel maneja la obtención y presentación de las estadísticas del usuario
class StatsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StatsRepository()

    private val _state = MutableStateFlow(StatsUiState())
    val state: StateFlow<StatsUiState> = _state.asStateFlow()

    // Carga las estadísticas detalladas desde el servidor
    fun loadStats(userId: String) {
        // Validación preventiva
        if (userId.isBlank()) {
            _state.update { it.copy(isLoading = false, errorMessage = "ID de usuario inválido") }
            return
        }
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            repository.fetchStats(userId)
                .onSuccess { data ->
                    // Guardamos por un lado los globales y por otro la lista de juegos
                    _state.update {
                        it.copy(
                            isLoading = false,
                            globalStats = data.global,
                            gameStats = data.game_stats
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(isLoading = false, errorMessage = err.message ?: "Error desconocido") }
                }
        }
    }
}
