package com.example.overdrivexandroid.feature_stats.domain

data class StatsResponse(
    val global: GlobalStatsSummary,
    val game_stats: List<GameStatSummary>
)

data class GlobalStatsSummary(
    val total_wins: Int = 0,
    val total_losses: Int = 0,
    val total_matches: Int = 0,
    val global_elo: Int = 1000,
    val global_rank: String = "Unranked"
)

data class GameStatSummary(
    val game_name: String,
    val game_slug: String = "",
    val elo: Int = 1000,
    val rank: String = "Unranked",
    val wins: Int = 0,
    val losses: Int = 0
)
