package com.example.overdrivexandroid.feature_friends.domain

data class Friend(
    val id: String,
    val username: String,
    val rank: String,
    val elo: Int,
    val online: Boolean,
    val avatar_url: String? = null
)

data class PendingRequest(
    val id: String, // request_id
    val sender_id: String,
    val username: String,
    val avatar_url: String? = null
)

data class FriendActionRequest(
    val request_id: String
)

data class AddFriendRequest(
    val username: String
)

data class GeneralResponse(
    val msg: String
)
