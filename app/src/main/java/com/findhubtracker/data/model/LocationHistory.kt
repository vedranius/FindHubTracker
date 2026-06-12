package com.findhubtracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "location_history")
data class LocationHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val trackerAddress: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val rssi: Int
)
