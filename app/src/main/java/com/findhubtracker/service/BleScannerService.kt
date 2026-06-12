package com.findhubtracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.findhubtracker.MainActivity
import com.findhubtracker.R
import com.findhubtracker.bluetooth.BleScanner
import com.findhubtracker.bluetooth.TrackerDetector
import com.findhubtracker.data.model.GeofenceZone
import com.findhubtracker.data.repository.TrackerRepository
import com.findhubtracker.util.Constants
import com.findhubtracker.util.LocationUtils
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class BleScannerService : Service() {

    companion object {
        private const val TAG = "BleScannerService"

        fun start(context: Context) {
            val intent = Intent(context, BleScannerService::class.java).apply {
                action = Constants.ACTION_START_SCAN
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, BleScannerService::class.java).apply {
                action = Constants.ACTION_STOP_SCAN
            }
            context.startService(intent)
        }
    }

    private lateinit var bleScanner: BleScanner
    private lateinit var trackerDetector: TrackerDetector
    private lateinit var repository: TrackerRepository
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var scanJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        bleScanner = BleScanner(this)
        trackerDetector = TrackerDetector(this)
        repository = TrackerRepository(this)

        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_SCAN -> {
                startForeground(Constants.SERVICE_NOTIFICATION_ID, createServiceNotification())
                startPeriodicScan()
                repository.setServiceRunning(true)
                Log.d(TAG, "Service started")
            }
            Constants.ACTION_STOP_SCAN -> {
                stopPeriodicScan()
                repository.setServiceRunning(false)
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
                Log.d(TAG, "Service stopped")
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopPeriodicScan()
        serviceScope.cancel()
    }

    private fun startPeriodicScan() {
        scanJob?.cancel()
        val interval = repository.getScanInterval()

        scanJob = serviceScope.launch {
            while (isActive) {
                performScan()
                delay(interval)
            }
        }
    }

    private fun stopPeriodicScan() {
        scanJob?.cancel()
        bleScanner.stopScan()
    }

    private fun performScan() {
        Log.d(TAG, "Starting periodic BLE scan")

        bleScanner.startScan { scanResult ->
            serviceScope.launch {
                val tracker = trackerDetector.onTrackerDetected(scanResult)
                checkGeofence(tracker.name, tracker.lastLatitude, tracker.lastLongitude)
            }
        }

        serviceScope.launch {
            delay(Constants.BLE_SCAN_TIMEOUT_MS)
            bleScanner.stopScan()
        }
    }

    private suspend fun checkGeofence(trackerName: String, latitude: Double, longitude: Double) {
        val zones = mutableListOf<GeofenceZone>()

        val zoneList = repository.allGeofenceZones.first()
        zones.addAll(zoneList)

        for (zone in zones) {
            if (!zone.isActive) continue

            val isInside = LocationUtils.isInsideGeofence(
                latitude, longitude,
                zone.latitude, zone.longitude,
                zone.radiusMeters
            )

            if (!isInside) {
                Log.d(TAG, "Tracker '$trackerName' is outside geofence '${zone.name}'")
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "FindHub Tracker pozadinski servis"
            }

            val alertChannel = NotificationChannel(
                Constants.ALERT_CHANNEL_ID,
                Constants.ALERT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Obavijesti kad tracker napusti geofence zonu"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(serviceChannel)
            notificationManager.createNotificationChannel(alertChannel)
        }
    }

    private fun createServiceNotification(): Notification {
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, BleScannerService::class.java).apply {
            action = Constants.ACTION_STOP_SCAN
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("FindHub Tracker")
            .setContentText("Skeniranje za trackerima u tijeku…")
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_notification, "Zaustavi", stopPendingIntent)
            .setOngoing(true)
            .build()
    }
}
