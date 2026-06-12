package com.findhubtracker.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.findhubtracker.data.db.AppDatabase
import com.findhubtracker.data.model.*
import kotlinx.coroutines.flow.Flow

class TrackerRepository(context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val trackerDao = db.trackerDao()
    private val geofenceDao = db.geofenceDao()
    private val historyDao = db.locationHistoryDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("findhub_prefs", Context.MODE_PRIVATE)

    val allTrackers: Flow<List<Tracker>> = trackerDao.getAllTrackers()
    val activeGeofenceZones: Flow<List<GeofenceZone>> = geofenceDao.getActiveZones()
    val allGeofenceZones: Flow<List<GeofenceZone>> = geofenceDao.getAllZones()

    suspend fun insertOrUpdateTracker(tracker: Tracker) {
        trackerDao.insertOrUpdate(tracker)
    }

    suspend fun getTracker(address: String): Tracker? {
        return trackerDao.getTrackerByAddress(address)
    }

    suspend fun deleteTracker(tracker: Tracker) {
        trackerDao.delete(tracker)
    }

    suspend fun updateTrackerGeofence(
        address: String,
        enabled: Boolean,
        latitude: Double,
        longitude: Double,
        radius: Float,
        name: String,
        interval: Long
    ) {
        trackerDao.updateGeofence(address, enabled, latitude, longitude, radius, name, interval)
    }

    suspend fun insertGeofenceZone(zone: GeofenceZone) {
        geofenceDao.insertOrUpdate(zone)
    }

    suspend fun getGeofenceZone(id: String): GeofenceZone? {
        return geofenceDao.getZoneById(id)
    }

    suspend fun deleteGeofenceZone(zone: GeofenceZone) {
        geofenceDao.delete(zone)
    }

    suspend fun addLocationHistory(entry: LocationHistory) {
        historyDao.insert(entry)
    }

    fun getLocationHistory(trackerAddress: String): Flow<List<LocationHistory>> {
        return historyDao.getHistoryForTracker(trackerAddress)
    }

    fun getScanInterval(): Long {
        return prefs.getLong("scan_interval", 300_000L)
    }

    fun setScanInterval(intervalMs: Long) {
        prefs.edit().putLong("scan_interval", intervalMs).apply()
    }

    fun getEmailConfig(): EmailConfig {
        return EmailConfig(
            recipientEmail = prefs.getString("email_recipient", "") ?: "",
            smtpServer = prefs.getString("smtp_server", "smtp.gmail.com") ?: "smtp.gmail.com",
            smtpPort = prefs.getString("smtp_port", "587") ?: "587",
            username = prefs.getString("smtp_username", "") ?: "",
            password = prefs.getString("smtp_password", "") ?: "",
            isEnabled = prefs.getBoolean("email_enabled", false)
        )
    }

    fun saveEmailConfig(config: EmailConfig) {
        prefs.edit().apply {
            putString("email_recipient", config.recipientEmail)
            putString("smtp_server", config.smtpServer)
            putString("smtp_port", config.smtpPort)
            putString("smtp_username", config.username)
            putString("smtp_password", config.password)
            putBoolean("email_enabled", config.isEnabled)
            apply()
        }
    }

    fun isServiceRunning(): Boolean {
        return prefs.getBoolean("service_running", false)
    }

    fun setServiceRunning(running: Boolean) {
        prefs.edit().putBoolean("service_running", running).apply()
    }
}
