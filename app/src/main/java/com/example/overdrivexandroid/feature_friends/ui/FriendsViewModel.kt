package com.example.overdrivexandroid.feature_friends.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.overdrivexandroid.feature_friends.data.FriendsRepository
import com.example.overdrivexandroid.feature_friends.domain.Friend
import com.example.overdrivexandroid.feature_friends.domain.PendingRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Estado de la pantalla de Amigos
data class FriendsState(
    val isLoading: Boolean = true,
    val friendsList: List<Friend> = emptyList(),          // Amigos ya aceptados
    val pendingRequests: List<PendingRequest> = emptyList(), // Peticiones que el usuario ha recibido
    val errorMessage: String? = null,
    val successMessage: String? = null
)

// FriendsViewModel gestiona la lista de amigos y las invitaciones recibidas o enviadas
class FriendsViewModel : ViewModel() {
    private val repository = FriendsRepository()

    private val _state = MutableStateFlow(FriendsState())
    val state: StateFlow<FriendsState> = _state.asStateFlow()

    init {
        loadData() // Al abrir la pantalla cargamos la lista automáticamente
    }

    // Descarga tanto la lista de amigos como las peticiones pendientes del servidor
    fun loadData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            
            val friendsResult = repository.getFriendsList()
            val pendingResult = repository.getPendingRequests()

            val friends = friendsResult.getOrDefault(emptyList())
            val pending = pendingResult.getOrDefault(emptyList())

            // Solo mostramos error si ambas peticiones fallan estrepitosamente
            if (friendsResult.isFailure && pendingResult.isFailure) {
                _state.update { it.copy(isLoading = false, errorMessage = "Error al cargar los amigos") }
            } else {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        friendsList = friends, 
                        pendingRequests = pending
                    ) 
                }
            }
        }
    }

    // Acepta una invitación de amistad recibida
    fun acceptRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.acceptRequest(requestId)
            result.onSuccess { msg ->
                _state.update { it.copy(successMessage = msg) }
                loadData() // Recargamos para que el usuario pase de "pendiente" a "amigo"
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    // Rechaza o borra una invitación de amistad recibida
    fun rejectRequest(requestId: String) {
        viewModelScope.launch {
            val result = repository.rejectRequest(requestId)
            result.onSuccess { msg ->
                _state.update { it.copy(successMessage = msg) }
                loadData() // Recargamos para ver cómo desaparece de la lista
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    // Envía una solicitud de amistad buscando al usuario por su nombre exacto
    fun sendRequest(username: String) {
        viewModelScope.launch {
            val result = repository.sendFriendRequest(username)
            result.onSuccess { msg ->
                _state.update { it.copy(successMessage = msg) }
            }.onFailure { error ->
                _state.update { it.copy(errorMessage = error.message) }
            }
        }
    }

    fun clearMessages() {
        _state.update { it.copy(errorMessage = null, successMessage = null) }
    }
}
