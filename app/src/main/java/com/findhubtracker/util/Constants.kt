package com.findhubtracker.util

object Constants {
    const val NOTIFICATION_CHANNEL_ID = "findhub_tracker_channel"
    const val NOTIFICATION_CHANNEL_NAME = "FindHub Tracker"
    const val ALERT_CHANNEL_ID = "geofence_alert_channel"
    const val ALERT_CHANNEL_NAME = "Geofence Alertovi"
    const val SERVICE_NOTIFICATION_ID = 1001
    const val ALERT_NOTIFICATION_ID = 2001

    const val ACTION_START_SCAN = "com.findhubtracker.ACTION_START_SCAN"
    const val ACTION_STOP_SCAN = "com.findhubtracker.ACTION_STOP_SCAN"

    const val DEFAULT_SCAN_INTERVAL_MS = 300_000L
    const val MIN_GEOFENCE_RADIUS_METERS = 100f
    const val MAX_GEOFENCE_RADIUS_METERS = 5000f
    const val DEFAULT_GEOFENCE_RADIUS_METERS = 500f

    const val BLE_SCAN_TIMEOUT_MS = 30_000L

    const val PREFS_NAME = "findhub_prefs"
    const val KEY_SCAN_INTERVAL = "scan_interval"
    const val KEY_EMAIL_RECIPIENT = "email_recipient"
    const val KEY_SMTP_SERVER = "smtp_server"
    const val KEY_SMTP_PORT = "smtp_port"
    const val KEY_SMTP_USERNAME = "smtp_username"
    const val KEY_SMTP_PASSWORD = "smtp_password"
    const val KEY_EMAIL_ENABLED = "email_enabled"
    const val KEY_SERVICE_RUNNING = "service_running"
}
