package com.findhubtracker.util

import android.location.Location
import java.text.SimpleDateFormat
import java.util.*

object LocationUtils {

    fun calculateDistance(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        return results[0]
    }

    fun isInsideGeofence(
        pointLat: Double, pointLon: Double,
        centerLat: Double, centerLon: Double,
        radiusMeters: Float
    ): Boolean {
        val distance = calculateDistance(pointLat, pointLon, centerLat, centerLon)
        return distance <= radiusMeters
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDistance(meters: Float): String {
        return if (meters < 1000) {
            "${meters.toInt()}m"
        } else {
            String.format("%.1fkm", meters / 1000)
        }
    }

    fun formatCoordinates(lat: Double, lon: Double): String {
        return String.format("%.6f, %.6f", lat, lon)
    }

    fun getGoogleMapsUrl(lat: Double, lon: Double): String {
        return "https://www.google.com/maps?q=$lat,$lon"
    }
}
