package com.findhubtracker.bluetooth

import android.os.ParcelUuid
import java.util.UUID

object TrackerUuids {

    val FMDN_SERVICE_UUID: ParcelUuid = ParcelUuid.fromString("0000FE2C-0000-1000-8000-00805F9B34FB")

    private val KNOWN_MANUFACTURER_IDS = mapOf(
        0x00E0 to "Google (Find Hub)",
        0x0310 to "Chipolo",
        0x0387 to "Pebblebee",
        0x0157 to "Motorola (Moto Tag)",
        0x0343 to "Xiaomi",
        0x0499 to "Eufy"
    )

    private val TRACKER_BRAND_KEYWORDS = mapOf(
        "chipolo" to "Chipolo",
        "pebblebee" to "Pebblebee",
        "moto tag" to "Moto Tag",
        "xiaomi" to "Xiaomi",
        "eufy" to "Eufy",
        "smart tag" to "Samsung",
        "airtag" to "Apple AirTag",
        "tile" to "Tile"
    )

    fun getFmdnServiceUuid(): ParcelUuid = FMDN_SERVICE_UUID

    fun identifyBrand(manufacturerId: Int, deviceName: String?): String {
        KNOWN_MANUFACTURER_IDS[manufacturerId]?.let { return it }

        deviceName?.lowercase()?.let { name ->
            for ((keyword, brand) in TRACKER_BRAND_KEYWORDS) {
                if (name.contains(keyword)) return brand
            }
        }

        return "Unknown Tracker"
    }

    fun isLikelyTracker(manufacturerId: Int, serviceUuids: List<ParcelUuid>, deviceName: String?): Boolean {
        if (serviceUuids.contains(FMDN_SERVICE_UUID)) return true
        if (KNOWN_MANUFACTURER_IDS.containsKey(manufacturerId)) return true

        deviceName?.lowercase()?.let { name ->
            for (keyword in TRACKER_BRAND_KEYWORDS.keys) {
                if (name.contains(keyword)) return true
            }
        }

        return false
    }
}
