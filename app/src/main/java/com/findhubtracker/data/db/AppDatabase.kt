package com.findhubtracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.findhubtracker.data.model.GeofenceZone
import com.findhubtracker.data.model.LocationHistory
import com.findhubtracker.data.model.Tracker

@Database(
    entities = [Tracker::class, GeofenceZone::class, LocationHistory::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackerDao(): TrackerDao
    abstract fun geofenceDao(): GeofenceDao
    abstract fun locationHistoryDao(): LocationHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE trackers ADD COLUMN geofenceEnabled INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE trackers ADD COLUMN geofenceLatitude REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE trackers ADD COLUMN geofenceLongitude REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE trackers ADD COLUMN geofenceRadiusMeters REAL NOT NULL DEFAULT 500.0")
                db.execSQL("ALTER TABLE trackers ADD COLUMN checkIntervalMs INTEGER NOT NULL DEFAULT 300000")
                db.execSQL("ALTER TABLE trackers ADD COLUMN geofenceName TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "findhub_tracker.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
