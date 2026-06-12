package com.findhubtracker.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class TrackerLocation(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: String,
    val battery_level: Int?,
    val is_online: Boolean
)

data class TrackerListResponse(
    val trackers: List<TrackerLocation>,
    val last_updated: String,
    val count: Int
)

data class StatusResponse(
    val status: String,
    val authenticated: Boolean,
    val last_refresh: String?,
    val tracker_count: Int
)

interface FindHubApi {
    @GET("/")
    suspend fun getStatus(): StatusResponse

    @GET("/api/trackers")
    suspend fun getTrackers(): TrackerListResponse

    @GET("/api/trackers/{trackerId}")
    suspend fun getTracker(@Path("trackerId") trackerId: String): TrackerLocation

    @POST("/api/refresh")
    suspend fun refreshTrackers(): Map<String, String>

    @GET("/api/config")
    suspend fun getConfig(): Map<String, Any>
}

object ApiClient {
    private var baseUrl: String = "http://10.0.2.2:8000"
    private var retrofit: Retrofit? = null
    private var api: FindHubApi? = null

    fun setBaseUrl(url: String) {
        baseUrl = url
        retrofit = null
        api = null
    }

    fun getApi(): FindHubApi {
        if (api == null) {
            retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            api = retrofit!!.create(FindHubApi::class.java)
        }
        return api!!
    }
}
