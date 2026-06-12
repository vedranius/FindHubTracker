package com.findhubtracker.data.remote

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackendRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("backend_prefs", Context.MODE_PRIVATE)

    var serverUrl: String
        get() = prefs.getString("server_url", "http://10.0.2.2:8000") ?: "http://10.0.2.2:8000"
        set(value) = prefs.edit().putString("server_url", value).apply()

    var autoRefresh: Boolean
        get() = prefs.getBoolean("auto_refresh", true)
        set(value) = prefs.edit().putBoolean("auto_refresh", value).apply()

    var refreshInterval: Int
        get() = prefs.getInt("refresh_interval", 300)
        set(value) = prefs.edit().putInt("refresh_interval", value).apply()

    init {
        ApiClient.setBaseUrl(serverUrl)
    }

    suspend fun getStatus(): Result<StatusResponse> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.getApi().getStatus()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTrackers(): Result<TrackerListResponse> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.getApi().getTrackers()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getTracker(trackerId: String): Result<TrackerLocation> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.getApi().getTracker(trackerId)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun refreshTrackers(): Result<Map<String, String>> = withContext(Dispatchers.IO) {
        try {
            val response = ApiClient.getApi().refreshTrackers()
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun updateServerUrl(url: String) {
        serverUrl = url
        ApiClient.setBaseUrl(url)
    }
}
