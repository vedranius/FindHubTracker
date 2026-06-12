package com.findhubtracker.bluetooth

import android.content.Context
import com.findhubtracker.data.model.LocationHistory
import com.findhubtracker.data.model.Tracker
import com.findhubtracker.data.repository.TrackerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrackerDetector(private val context: Context) {

    private val repository = TrackerRepository(context)

    suspend fun onTrackerDetected(scanResult: ScanResultData): Tracker {
        val existing = repository.getTracker(scanResult.address)

        val tracker = Tracker(
            address = scanResult.address,
            name = scanResult.name,
            brand = scanResult.brand,
            lastLatitude = scanResult.latitude,
            lastLongitude = scanResult.longitude,
            lastSeenTimestamp = scanResult.timestamp,
            rssi = scanResult.rssi,
            isInsideGeofence = existing?.isInsideGeofence ?: true
        )

        repository.insertOrUpdateTracker(tracker)

        repository.addLocationHistory(
            LocationHistory(
                trackerAddress = scanResult.address,
                latitude = scanResult.latitude,
                longitude = scanResult.longitude,
                timestamp = scanResult.timestamp,
                rssi = scanResult.rssi
            )
        )

        return tracker
    }

    fun getAllTrackers(): Flow<List<Tracker>> = repository.allTrackers

    suspend fun getTracker(address: String): Tracker? = repository.getTracker(address)

    suspend fun updateGeofenceStatus(address: String, isInside: Boolean) {
        val tracker = repository.getTracker(address) ?: return
        repository.insertOrUpdateTracker(tracker.copy(isInsideGeofence = isInside))
    }
}
