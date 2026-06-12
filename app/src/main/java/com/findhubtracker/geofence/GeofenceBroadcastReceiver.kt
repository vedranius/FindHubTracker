package com.findhubtracker.geofence

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.findhubtracker.MainActivity
import com.findhubtracker.R
import com.findhubtracker.data.model.EmailConfig
import com.findhubtracker.data.model.GeofenceZone
import com.findhubtracker.data.repository.TrackerRepository
import com.findhubtracker.email.EmailSender
import com.findhubtracker.util.Constants
import com.findhubtracker.util.LocationUtils
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "GeofenceReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            Log.e(TAG, "Geofencing error: ${geofencingEvent.errorCode}")
            return
        }

        val transitionType = geofencingEvent.geofenceTransition
        val triggeredGeofences = geofencingEvent.triggeringGeofences ?: return

        val repository = TrackerRepository(context)

        CoroutineScope(Dispatchers.IO).launch {
            for (geofence in triggeredGeofences) {
                val zoneId = geofence.requestId
                val zone = repository.getGeofenceZone(zoneId) ?: continue

                val trackers = repository.allTrackers
                trackers.collect { trackerList ->
                    for (tracker in trackerList) {
                        val isInside = LocationUtils.isInsideGeofence(
                            tracker.lastLatitude, tracker.lastLongitude,
                            zone.latitude, zone.longitude,
                            zone.radiusMeters
                        )

                        val wasInside = tracker.isInsideGeofence

                        when (transitionType) {
                            Geofence.GEOFENCE_TRANSITION_EXIT -> {
                                if (wasInside) {
                                    Log.d(TAG, "Tracker '${tracker.name}' LEFT geofence '${zone.name}'")
                                    repository.insertOrUpdateTracker(
                                        tracker.copy(isInsideGeofence = false)
                                    )
                                    showAlertNotification(
                                        context,
                                        tracker.name,
                                        "je napustio geofence zonu '${zone.name}'",
                                        tracker.lastLatitude,
                                        tracker.lastLongitude
                                    )
                                    sendAlertEmail(
                                        context,
                                        repository,
                                        tracker.name,
                                        tracker.lastLatitude,
                                        tracker.lastLongitude,
                                        "LEFT",
                                        zone.name
                                    )
                                }
                            }
                            Geofence.GEOFENCE_TRANSITION_ENTER -> {
                                if (!wasInside) {
                                    Log.d(TAG, "Tracker '${tracker.name}' ENTERED geofence '${zone.name}'")
                                    repository.insertOrUpdateTracker(
                                        tracker.copy(isInsideGeofence = true)
                                    )
                                    showAlertNotification(
                                        context,
                                        tracker.name,
                                        "je ušao u geofence zonu '${zone.name}'",
                                        tracker.lastLatitude,
                                        tracker.lastLongitude
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun showAlertNotification(
        context: Context,
        trackerName: String,
        message: String,
        latitude: Double,
        longitude: Double
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = android.app.Notification.Builder(context, Constants.ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Geofence Alert")
            .setContentText("$trackerName $message")
            .setStyle(
                android.app.Notification.BigTextStyle()
                    .bigText("$trackerName $message\nLokacija: ${LocationUtils.formatCoordinates(latitude, longitude)}\nVrijeme: ${LocationUtils.formatTimestamp(System.currentTimeMillis())}")
            )
            .setPriority(android.app.Notification.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            Constants.ALERT_NOTIFICATION_ID + System.currentTimeMillis().toInt(),
            notification
        )
    }

    private fun sendAlertEmail(
        context: Context,
        repository: TrackerRepository,
        trackerName: String,
        latitude: Double,
        longitude: Double,
        event: String,
        zoneName: String
    ) {
        val emailConfig = repository.getEmailConfig()
        if (!emailConfig.isEnabled) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val mapsUrl = LocationUtils.getGoogleMapsUrl(latitude, longitude)
                val subject = "FindHub Alert: $trackerName - $event"
                val body = """
                    |Tracker Alert
                    |
                    |Tracker: $trackerName
                    |Event: $event geofence zone '$zoneName'
                    |Lokacija: ${LocationUtils.formatCoordinates(latitude, longitude)}
                    |Google Maps: $mapsUrl
                    |Vrijeme: ${LocationUtils.formatTimestamp(System.currentTimeMillis())}
                    |
                    |--- FindHub Tracker App
                """.trimMargin()

                EmailSender.sendEmail(
                    recipient = emailConfig.recipientEmail,
                    subject = subject,
                    body = body,
                    smtpServer = emailConfig.smtpServer,
                    smtpPort = emailConfig.smtpPort,
                    username = emailConfig.username,
                    password = emailConfig.password
                )

                Log.d(TAG, "Alert email sent for tracker $trackerName")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send alert email", e)
            }
        }
    }
}
