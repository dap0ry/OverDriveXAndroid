package com.example.overdrivexandroid.feature_characters.data

import com.example.overdrivexandroid.core.network.RetrofitClient
import com.example.overdrivexandroid.feature_characters.domain.UpdateAvatarRequest
import com.example.overdrivexandroid.feature_characters.domain.UpdateAvatarResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// CharactersRepository envía la imagen del personaje elegido a la API.
class CharactersRepository {
    private val api = RetrofitClient.instance.create(CharactersApiService::class.java)

    // Actualiza el avatar del usuario enviando la URL del icono (busto) 
    // y del fullChar (imagen de cuerpo entero para la pantalla de VS)
    suspend fun updateAvatar(avatarUrl: String, fullCharUrl: String): Result<UpdateAvatarResponse> = withContext(Dispatchers.IO) {
        try {
            val response = api.updateAvatar(UpdateAvatarRequest(avatarUrl, fullCharUrl))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al actualizar avatar: HTTP ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
