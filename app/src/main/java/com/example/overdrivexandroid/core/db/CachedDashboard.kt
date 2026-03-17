package com.example.overdrivexandroid.core.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity that caches the minimal dashboard data needed for offline display.
 * Only one row is ever stored (id = 1), replaced on every successful API call.
 */
@Entity(tableName = "cached_dashboard")
data class CachedDashboard(
    @PrimaryKey val id: Int = 1,
    val globalRank: String,
    val level: Int,
    val globalElo: Int,
    val totalWins: Int,
    val totalTournaments: Int,
    /** JSON-serialized List<MatchHistory> */
    val lastMatchesJson: String
)
