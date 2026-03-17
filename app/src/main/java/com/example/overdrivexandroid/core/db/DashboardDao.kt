package com.example.overdrivexandroid.core.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DashboardDao {

    @Query("SELECT * FROM cached_dashboard WHERE id = 1")
    suspend fun get(): CachedDashboard?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(data: CachedDashboard)

    @Query("DELETE FROM cached_dashboard")
    suspend fun clear()
}
