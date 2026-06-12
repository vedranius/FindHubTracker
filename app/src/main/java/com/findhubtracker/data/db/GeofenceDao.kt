package com.findhubtracker.data.db

import androidx.room.*
import com.findhubtracker.data.model.GeofenceZone
import kotlinx.coroutines.flow.Flow

@Dao
interface GeofenceDao {
    @Query("SELECT * FROM geofence_zones WHERE isActive = 1")
    fun getActiveZones(): Flow<List<GeofenceZone>>

    @Query("SELECT * FROM geofence_zones")
    fun getAllZones(): Flow<List<GeofenceZone>>

    @Query("SELECT * FROM geofence_zones WHERE id = :id")
    suspend fun getZoneById(id: String): GeofenceZone?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(zone: GeofenceZone)

    @Delete
    suspend fun delete(zone: GeofenceZone)

    @Query("DELETE FROM geofence_zones")
    suspend fun deleteAll()
}
