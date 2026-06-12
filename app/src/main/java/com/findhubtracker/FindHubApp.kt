package com.findhubtracker

import android.app.Application
import com.findhubtracker.data.db.AppDatabase
import com.findhubtracker.data.repository.TrackerRepository

class FindHubApp : Application() {

    lateinit var repository: TrackerRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = TrackerRepository(this)
    }
}
