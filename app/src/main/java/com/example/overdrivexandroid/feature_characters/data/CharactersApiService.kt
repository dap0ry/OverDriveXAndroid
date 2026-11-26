package com.example.overdrivexandroid.feature_characters.data

import com.example.overdrivexandroid.feature_characters.domain.UpdateAvatarRequest
import com.example.overdrivexandroid.feature_characters.domain.UpdateAvatarResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface CharactersApiService {

    @POST("user/avatar")
    suspend fun updateAvatar(@Body request: UpdateAvatarRequest): Response<UpdateAvatarResponse>
}
