package com.example.wifisignallogger.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.wifisignallogger.data.model.Location
import com.example.wifisignallogger.data.model.WifiReading

@Database(entities = [Location::class, WifiReading::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun locationDao(): LocationDao
    abstract fun wifiReadingDao(): WifiReadingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wifi_signal_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}