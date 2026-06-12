package com.findhubtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "trackers")
data class Tracker(
    @PrimaryKey val address: String,
    val name: String,
    val brand: String,
    val lastLatitude: Double,
    val lastLongitude: Double,
    val lastSeenTimestamp: Long,
    val rssi: Int,
    val isInsideGeofence: Boolean = true,
    val geofenceEnabled: Boolean = false,
    val geofenceLatitude: Double = 0.0,
    val geofenceLongitude: Double = 0.0,
    val geofenceRadiusMeters: Float = 500f,
    val checkIntervalMs: Long = 300_000L,
    val geofenceName: String = ""
)
