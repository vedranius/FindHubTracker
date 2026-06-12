package com.findhubtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "geofence_zones")
data class GeofenceZone(
    @PrimaryKey val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val isActive: Boolean = true
)
