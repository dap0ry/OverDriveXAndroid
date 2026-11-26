package com.example.overdrivexandroid.feature_matchmaking.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.overdrivexandroid.core.session.SessionManager
import com.example.overdrivexandroid.feature_matchmaking.data.MatchmakingRepository
import com.example.overdrivexandroid.feature_matchmaking.domain.MatchmakingGame
import com.example.overdrivexandroid.feature_matchmaking.domain.JoinRequest
import com.example.overdrivexandroid.feature_matchmaking.domain.MatchStatusResponse
import com.example.overdrivexandroid.feature_matchmaking.domain.ReportRequest
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Define en qué pantalla del flujo de matchmaking estamos actualmente
enum class MatchmakingScreenState {
    SELECTOR,  // Elige juegos
    SEARCHING, // Buscando oponente (rueda girando)
    VERSUS,    // Pantalla de VS (oponente encontrado)
    DISPUTE    // Conflicto de resultados (requiere admin)
}

// Estado centralizado de la UI de Matchmaking
data class MatchmakingState(
    val screenState: MatchmakingScreenState = MatchmakingScreenState.SELECTOR,
    val availableGames: List<MatchmakingGame> = listOf(
        MatchmakingGame("street-fighter-6", "Street Fighter 6"),
        MatchmakingGame("tekken-8", "Tekken 8"),
        MatchmakingGame("dragon-ball-fighterz", "DB FighterZ"),
        MatchmakingGame("guilty-gear-strive", "Guilty Gear Strive")
    ),
    // Guardamos los slugs (IDs) de los juegos que el usuario ha marcado para buscar
    val selectedGameSlugs: Set<String> = emptySet(),
    val currentMatchId: String? = null,
    val matchData: MatchStatusResponse? = null,
    val activeTimerSeconds: Int = 15 * 60, // 15 mins por defecto para jugar el set
    val errorMessage: String? = null,
    
    // Datos cargados desde SessionManager para renderizar nuestro lado de la pantalla VS
    val myUsername: String = "TÚ",
    val myFullCharUrl: String? = null
)

// MatchmakingViewModel gestiona la cola de búsqueda, el polling (preguntar al servidor cada X segundos)
// y el reporte de resultados al finalizar la partida.
class MatchmakingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = MatchmakingRepository()

    private val _state = MutableStateFlow(MatchmakingState())
    val state: StateFlow<MatchmakingState> = _state.asStateFlow()

    // Guardamos referencias a las corrutinas para poder cancelarlas cuando haga falta
    private var pollingJob: Job? = null
    private var timerJob: Job? = null

    init {
        // Al arrancar, cargamos el personaje del usuario desde la sesión para la pantalla VS
        viewModelScope.launch {
            val context = getApplication<Application>()
            val fullChar = SessionManager.getFullCharUrl(context)
            _state.update { it.copy(myFullCharUrl = fullChar) }
            
            // Comprobamos si el usuario ya estaba en cola o en partida al entrar
            checkStatus() 
        }
    }

    // Selecciona o deselecciona un juego de la lista
    fun toggleGameSelection(slug: String) {
        _state.update {
            val newSelection = if (it.selectedGameSlugs.contains(slug)) {
                it.selectedGameSlugs - slug // Quitar si ya estaba
            } else {
                it.selectedGameSlugs + slug // Añadir si no estaba
            }
            it.copy(selectedGameSlugs = newSelection, errorMessage = null)
        }
    }

    // Inicia la búsqueda de partida enviando los juegos seleccionados al servidor
    fun startSearch() {
        val selected = _state.value.selectedGameSlugs.toList()
        if (selected.isEmpty()) {
            _state.update { it.copy(errorMessage = "Selecciona al menos un juego.") }
            return
        }

        viewModelScope.launch {
            val result = repository.joinQueue(selected)
            result.onSuccess {
                // Pasamos a la pantalla de búsqueda y empezamos a preguntar al servidor
                _state.update { it.copy(screenState = MatchmakingScreenState.SEARCHING, errorMessage = null) }
                startPolling()
            }.onFailure { err ->
                _state.update { it.copy(errorMessage = err.message) }
            }
        }
    }

    // Cancela la búsqueda y vuelve a la pantalla de selección
    fun cancelSearch() {
        viewModelScope.launch {
            repository.cancelSearch()
            stopPolling()
            _state.update { 
                it.copy(
                    screenState = MatchmakingScreenState.SELECTOR,
                    currentMatchId = null,
                    matchData = null
                ) 
            }
        }
    }

    // Inicia un bucle infinito (corrutina) que comprueba el estado cada 3 segundos.
    // Es necesario porque los WebSockets aún no están implementados, usamos Long Polling.
    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (true) {
                checkStatus()
                delay(3000) // Espera 3s entre peticiones para no saturar el servidor
            }
        }
    }

    private fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    // Llama al API para ver si ya nos han encontrado rival
    private suspend fun checkStatus() {
        val result = repository.getStatus()
        result.onSuccess { data ->
            when (data.status) {
                "match_found" -> {
                    // ¡Rivales encontrados! Paramos de buscar y mostramos la pantalla de VS
                    stopPolling()
                    _state.update { 
                        it.copy(
                            screenState = MatchmakingScreenState.VERSUS,
                            currentMatchId = data.match_id,
                            matchData = data
                        ) 
                    }
                    startMatchTimer()
                }
                "in_queue" -> {
                    // Seguimos en cola
                    _state.update { it.copy(screenState = MatchmakingScreenState.SEARCHING) }
                    if (pollingJob == null) startPolling()
                }
                else -> {
                    // Ni en cola ni en partida (estado reseteado)
                    _state.update { it.copy(screenState = MatchmakingScreenState.SELECTOR) }
                    stopPolling()
                }
            }
        }
    }

    // Arranca un temporizador de 15 minutos para que los jugadores terminen su set
    private fun startMatchTimer() {
        timerJob?.cancel()
        _state.update { it.copy(activeTimerSeconds = 15 * 60) }
        
        timerJob = viewModelScope.launch {
            while (_state.value.activeTimerSeconds > 0) {
                delay(1000)
                _state.update { it.copy(activeTimerSeconds = it.activeTimerSeconds - 1) }
            }
        }
    }

    // Envía el resultado (WIN/LOSS) de la partida al servidor.
    // Si ambos jugadores mandan resultados contradictorios, el server devuelve "disputed".
    fun reportResult(resultType: String, onSuccessCallback: () -> Unit) {
        val matchId = _state.value.currentMatchId ?: return

        viewModelScope.launch {
            val result = repository.reportResult(matchId, resultType)
            result.onSuccess { response ->
                timerJob?.cancel()
                if (response.status == "success") {
                    onSuccessCallback() // Vuelve al dashboard si todo va bien
                } else if (response.status == "disputed") {
                    // Hay conflicto (ej: ambos dijeron WIN)
                    _state.update { it.copy(screenState = MatchmakingScreenState.DISPUTE) }
                } else {
                    _state.update { it.copy(errorMessage = response.msg) }
                }
            }.onFailure { err ->
                _state.update { it.copy(errorMessage = err.message) }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}
