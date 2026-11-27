package com.example.overdrivexandroid.feature_friends.data

import com.example.overdrivexandroid.feature_friends.domain.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface FriendsApiService {
    @GET("friends/list")
    suspend fun getFriendsList(): Response<List<Friend>>

    @GET("friends/pending")
    suspend fun getPendingRequests(): Response<List<PendingRequest>>

    @POST("friends/accept")
    suspend fun acceptRequest(@Body request: FriendActionRequest): Response<GeneralResponse>

    @POST("friends/reject")
    suspend fun rejectRequest(@Body request: FriendActionRequest): Response<GeneralResponse>

    @POST("friends/request")
    suspend fun sendFriendRequest(@Body request: AddFriendRequest): Response<GeneralResponse>
}
