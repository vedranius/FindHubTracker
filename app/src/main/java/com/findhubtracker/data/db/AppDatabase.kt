package com.findhubtracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.findhubtracker.data.model.GeofenceZone
import com.findhubtracker.data.model.LocationHistory
import com.findhubtracker.data.model.Tracker

@Database(
    entities = [Tracker::class, GeofenceZone::class, LocationHistory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackerDao(): TrackerDao
    abstract fun geofenceDao(): GeofenceDao
    abstract fun locationHistoryDao(): LocationHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "findhub_tracker.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
