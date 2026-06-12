package com.findhubtracker.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.Context
import android.location.Location
import android.os.ParcelUuid
import android.util.Log
import com.findhubtracker.util.Constants
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BleScanner(private val context: Context) {

    companion object {
        private const val TAG = "BleScanner"
    }

    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private val bluetoothAdapter get() = bluetoothManager.adapter
    private val scanner get() = bluetoothAdapter?.bluetoothLeScanner
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private var isScanning = false
    private val _scanResults = MutableStateFlow<List<ScanResultData>>(emptyList())
    val scanResults: StateFlow<List<ScanResultData>> = _scanResults

    private val discoveredTrackers = mutableMapOf<String, ScanResultData>()
    private var scanJob: Job? = null
    private var onTrackerFound: ((ScanResultData) -> Unit)? = null

    @SuppressLint("MissingPermission")
    fun startScan(onTracker: (ScanResultData) -> Unit) {
        if (isScanning) return
        if (scanner == null) {
            Log.e(TAG, "Bluetooth LE scanner not available")
            return
        }

        onTrackerFound = onTracker
        isScanning = true
        discoveredTrackers.clear()

        val filters = listOf(
            ScanFilter.Builder()
                .setServiceUuid(TrackerUuids.getFmdnServiceUuid())
                .build()
        )

        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setReportDelay(0)
            .build()

        Log.d(TAG, "Starting BLE scan")
        scanner?.startScan(filters, settings, scanCallback)

        scanJob = CoroutineScope(Dispatchers.IO).launch {
            delay(Constants.BLE_SCAN_TIMEOUT_MS)
            stopScan()
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (!isScanning) return
        isScanning = false
        scanJob?.cancel()

        try {
            scanner?.stopScan(scanCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping scan", e)
        }

        Log.d(TAG, "BLE scan stopped. Found ${discoveredTrackers.size} trackers")
        _scanResults.value = discoveredTrackers.values.toList()
    }

    @SuppressLint("MissingPermission")
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val address = device.address
            val rssi = result.rssi
            val scanRecord = result.scanRecord

            var manufacturerId = -1
            var deviceName: String? = null

            scanRecord?.let { record ->
                deviceName = record.deviceName

                record.manufacturerSpecificData?.let { msd ->
                    for (i in 0 until msd.size()) {
                        manufacturerId = msd.keyAt(i)
                    }
                }
            }

            val serviceUuids = scanRecord?.serviceUuids?.map { it.uuid } ?: emptyList()

            if (TrackerUuids.isLikelyTracker(manufacturerId, serviceUuids.map { ParcelUuid.fromString(it.toString()) }, deviceName)) {
                val brand = TrackerUuids.identifyBrand(manufacturerId, deviceName)

                getCurrentLocation { location ->
                    val trackerData = ScanResultData(
                        address = address,
                        name = deviceName ?: brand,
                        brand = brand,
                        rssi = rssi,
                        latitude = location?.latitude ?: 0.0,
                        longitude = location?.longitude ?: 0.0,
                        timestamp = System.currentTimeMillis()
                    )

                    discoveredTrackers[address] = trackerData
                    _scanResults.value = discoveredTrackers.values.toList()
                    onTrackerFound?.invoke(trackerData)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e(TAG, "Scan failed with error code: $errorCode")
            isScanning = false
        }
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(callback: (Location?) -> Unit) {
        try {
            val cancellationToken = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                callback(location)
            }.addOnFailureListener {
                callback(null)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "No location permission", e)
            callback(null)
        }
    }

    fun isCurrentlyScanning(): Boolean = isScanning
}

data class ScanResultData(
    val address: String,
    val name: String,
    val brand: String,
    val rssi: Int,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
)
