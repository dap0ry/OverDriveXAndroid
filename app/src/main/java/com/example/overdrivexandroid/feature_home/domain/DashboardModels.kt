package com.example.overdrivexandroid.feature_home.domain

data class DashboardResponse(
    val username: String,
    val level: Int,
    val xp: Int,
    val total_tournaments: Int,
    val stats: GlobalStats,
    val last_matches: List<MatchHistory>,
    val friends: FriendStats,
    val avatar_url: String? = null,
    val game_stats: List<GameStats> = emptyList()
)

data class GlobalStats(
    val global_elo: Int,
    val global_rank: String,
    val total_wins: Int,
    val total_losses: Int,
    val total_matches: Int
)

data class GameStats(
    val game_name: String,
    val rank: String,
    val elo: Int,
    val wins: Int,
    val losses: Int,
    val game_slug: String? = null
)

data class MatchHistory(
    val id: String,
    val game_name: String,
    val result: String, // WIN, LOSS
    val elo_before: Int,
    val elo_after: Int,
    val created_at: String
)

data class FriendStats(
    val total: Int,
    val online: Int,
    val pending: Int
)
