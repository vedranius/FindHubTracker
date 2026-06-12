package com.findhubtracker.data.db

import androidx.room.*
import com.findhubtracker.data.model.LocationHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationHistoryDao {
    @Query("SELECT * FROM location_history WHERE trackerAddress = :address ORDER BY timestamp DESC LIMIT 100")
    fun getHistoryForTracker(address: String): Flow<List<LocationHistory>>

    @Insert
    suspend fun insert(entry: LocationHistory)

    @Query("DELETE FROM location_history WHERE trackerAddress = :address")
    suspend fun deleteHistoryForTracker(address: String)

    @Query("DELETE FROM location_history")
    suspend fun deleteAll()
}
