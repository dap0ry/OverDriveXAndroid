package com.example.overdrivexandroid.feature_characters.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.overdrivexandroid.core.session.SessionManager
import com.example.overdrivexandroid.feature_characters.data.CharactersRepository
import com.example.overdrivexandroid.feature_characters.domain.Character
import com.example.overdrivexandroid.feature_characters.domain.GameRoster
import com.example.overdrivexandroid.feature_characters.domain.WEB_ROSTERS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la pantalla de selección de personajes
data class CharactersState(
    val rosters: List<GameRoster> = WEB_ROSTERS,       // Datos estáticos de los personajes por juego
    val selectedGameIndex: Int = 0,                    // Qué pestaña/juego está seleccionado actualmente
    val selectedCharacter: Character? = null,          // El personaje que el usuario ha tocado
    val isSaving: Boolean = false,                     // Indicador de guardando en el servidor
    val saveSuccess: Boolean = false,                  // True cuando se guardó correctamente
    val errorMessage: String? = null
)

// CharactersViewModel maneja la selección local y la subida de los nuevos avatares al servidor
class CharactersViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CharactersRepository()

    private val _state = MutableStateFlow(CharactersState())
    val state: StateFlow<CharactersState> = _state.asStateFlow()

    // Cambia la pestaña de juego (ej: de Tekken a Street Fighter)
    fun selectGame(index: Int) {
        _state.update { 
            it.copy(
                selectedGameIndex = index,
                selectedCharacter = null, // Al cambiar de juego, borramos el personaje seleccionado
                saveSuccess = false,
                errorMessage = null
            ) 
        }
    }

    // Marca un personaje como seleccionado (lo resalta en la UI)
    fun selectCharacter(character: Character) {
        _state.update {
            it.copy(
                selectedCharacter = character,
                saveSuccess = false,
                errorMessage = null
            )
        }
    }

    // Guarda el personaje seleccionado como avatar principal del usuario
    fun saveSelectedCharacter() {
        val character = _state.value.selectedCharacter ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, errorMessage = null, saveSuccess = false) }

            // Enviamos un POST a la API con las dos URLs: el busto circular y la imagen de cuerpo entero
            val result = repository.updateAvatar(character.icon, character.image)
            
            result.onSuccess {
                // Guardamos en DataStore (localmente) para que Dashboard y Matchmaking lo actualicen al instante
                // sin necesidad de volver a pedírselo al API
                SessionManager.saveAvatarUrl(getApplication(), character.icon, character.image)
                
                _state.update { it.copy(isSaving = false, saveSuccess = true) }
            }.onFailure { error ->
                _state.update { it.copy(isSaving = false, errorMessage = error.message) }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(saveSuccess = false, errorMessage = null) }
    }
}
