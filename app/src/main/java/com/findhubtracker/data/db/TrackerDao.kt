package com.findhubtracker.data.db

import androidx.room.*
import com.findhubtracker.data.model.Tracker
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    @Query("SELECT * FROM trackers ORDER BY lastSeenTimestamp DESC")
    fun getAllTrackers(): Flow<List<Tracker>>

    @Query("SELECT * FROM trackers WHERE address = :address")
    suspend fun getTrackerByAddress(address: String): Tracker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(tracker: Tracker)

    @Delete
    suspend fun delete(tracker: Tracker)

    @Query("DELETE FROM trackers")
    suspend fun deleteAll()
}
