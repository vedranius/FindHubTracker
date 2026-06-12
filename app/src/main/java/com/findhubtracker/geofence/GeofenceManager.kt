package com.findhubtracker.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.findhubtracker.data.model.GeofenceZone
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.LocationServices

class GeofenceManager(private val context: Context) {

    companion object {
        private const val TAG = "GeofenceManager"
        const val GEOFENCE_TRANSITION_REQUEST_CODE = 1002
    }

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    @SuppressLint("MissingPermission")
    fun addGeofence(zone: GeofenceZone) {
        val geofence = Geofence.Builder()
            .setRequestId(zone.id)
            .setCircularRegion(zone.latitude, zone.longitude, zone.radiusMeters)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or
                        Geofence.GEOFENCE_TRANSITION_EXIT
            )
            .setLoiteringDelay(30_000)
            .setNotificationResponsiveness(1_000)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent())
            .addOnSuccessListener {
                Log.d(TAG, "Geofence '${zone.id}' added successfully")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to add geofence: ${e.message}")
            }
    }

    fun removeGeofence(zoneId: String) {
        geofencingClient.removeGeofences(listOf(zoneId))
            .addOnSuccessListener { Log.d(TAG, "Geofence '$zoneId' removed") }
            .addOnFailureListener { e -> Log.e(TAG, "Remove failed: ${e.message}") }
    }

    fun removeAllGeofences() {
        geofencingClient.removeGeofences(getGeofencePendingIntent())
            .addOnSuccessListener { Log.d(TAG, "All geofences removed") }
            .addOnFailureListener { e -> Log.e(TAG, "Remove all failed: ${e.message}") }
    }

    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        return PendingIntent.getBroadcast(
            context,
            GEOFENCE_TRANSITION_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
}
