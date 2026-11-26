package com.example.overdrivexandroid.feature_matchmaking.domain

data class MatchStatusResponse(
    val status: String,           // "no_queue", "in_queue", "match_found"
    val match_id: String? = null,
    val opponent_name: String? = null,
    val game_name: String? = null,
    val player_avatar: String? = null,
    val opponent_avatar: String? = null,
    val player_full: String? = null,
    val opponent_full: String? = null
)

data class JoinRequest(
    val games: List<String>
)

data class ReportRequest(
    val match_id: String,
    val result: String  // "win" or "loss"
)

data class ReportResponse(
    val status: String,  // "success", "pending", "disputed"
    val msg: String? = null
)

data class MatchmakingGame(
    val slug: String,
    val name: String
)

val AVAILABLE_GAMES = listOf(
    MatchmakingGame("street-fighter-6", "STREET FIGHTER 6"),
    MatchmakingGame("tekken-8", "TEKKEN 8"),
    MatchmakingGame("dragon-ball-fighterz", "DB FIGHTER Z"),
    MatchmakingGame("guilty-gear-strive", "GUILTY GEAR STRIVE"),
    MatchmakingGame("skullgirls-2nd-encore", "SKULLGIRLS 2ND")
)
