package com.example.overdrivexandroid.feature_friends.data

import com.example.overdrivexandroid.core.network.RetrofitClient
import com.example.overdrivexandroid.feature_friends.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// FriendsRepository se encarga de llamar a los endpoints de la API relacionados con amigos
class FriendsRepository {
    private val api = RetrofitClient.instance.create(FriendsApiService::class.java)

    // Obtiene la lista de amigos ya aceptados
    suspend fun getFriendsList(): Result<List<Friend>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getFriendsList()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Fallo al obtener la lista de amigos"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Obtiene las solicitudes de amistad recibidas que aún no han sido contestadas
    suspend fun getPendingRequests(): Result<List<PendingRequest>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getPendingRequests()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("Fallo al obtener solicitudes pendientes"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Responde afirmativamente a una solicitud de amistad enviando su ID
    suspend fun acceptRequest(requestId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.acceptRequest(FriendActionRequest(requestId))
            if (response.isSuccessful) {
                Result.success(response.body()?.msg ?: "Aceptado")
            } else {
                Result.failure(Exception("Error al aceptar solicitud"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Elimina o deniega una solicitud de amistad
    suspend fun rejectRequest(requestId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.rejectRequest(FriendActionRequest(requestId))
            if (response.isSuccessful) {
                Result.success(response.body()?.msg ?: "Rechazado")
            } else {
                Result.failure(Exception("Error al rechazar solicitud"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Busca a un usuario por nombre y le envía una invitación
    suspend fun sendFriendRequest(username: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = api.sendFriendRequest(AddFriendRequest(username))
            if (response.isSuccessful) {
                Result.success(response.body()?.msg ?: "Solicitud enviada")
            } else {
                // Leemos el mensaje de error del servidor (por ejemplo, "Usuario no existe")
                val errorBody = response.errorBody()?.string() ?: "Error al enviar solicitud"
                Result.failure(Exception(errorBody))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
