package com.findhubtracker.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.findhubtracker.data.repository.TrackerRepository

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val repository = TrackerRepository(context)
            if (repository.isServiceRunning()) {
                Log.d("BootReceiver", "Restarting BleScannerService after boot")
                BleScannerService.start(context)
            }
        }
    }
}
