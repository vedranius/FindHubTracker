package com.findhubtracker.service

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import com.findhubtracker.R
import com.findhubtracker.util.Constants
import com.findhubtracker.util.LocationUtils

object NotificationService {

    fun showTrackerAlert(
        context: Context,
        trackerName: String,
        message: String,
        latitude: Double,
        longitude: Double
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = Notification.Builder(context, Constants.ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Tracker Alert: $trackerName")
            .setContentText(message)
            .setStyle(
                Notification.BigTextStyle()
                    .bigText(
                        "$message\n" +
                                "Lokacija: ${LocationUtils.formatCoordinates(latitude, longitude)}\n" +
                                "Vrijeme: ${LocationUtils.formatTimestamp(System.currentTimeMillis())}"
                    )
            )
            .setPriority(Notification.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }
}
