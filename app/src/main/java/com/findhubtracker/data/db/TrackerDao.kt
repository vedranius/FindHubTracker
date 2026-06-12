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

    @Query("UPDATE trackers SET geofenceEnabled = :enabled, geofenceLatitude = :lat, geofenceLongitude = :lon, geofenceRadiusMeters = :radius, geofenceName = :name, checkIntervalMs = :interval WHERE address = :address")
    suspend fun updateGeofence(address: String, enabled: Boolean, lat: Double, lon: Double, radius: Float, name: String, interval: Long)
}
