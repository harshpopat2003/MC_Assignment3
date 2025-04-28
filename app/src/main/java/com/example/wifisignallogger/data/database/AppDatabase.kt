package com.example.wifisignallogger.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.wifisignallogger.data.model.Location
import com.example.wifisignallogger.data.model.WifiReading

/**
 * Room database class for storing Location and WifiReading entities.
 * Provides DAOs for accessing Location and WifiReading data.
 */
@Database(entities = [Location::class, WifiReading::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Abstract method to get Location DAO
    abstract fun locationDao(): LocationDao

    // Abstract method to get WifiReading DAO
    abstract fun wifiReadingDao(): WifiReadingDao

    companion object {
        // Singleton instance of the database
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns the singleton instance of the AppDatabase.
         * If not already created, it builds a new instance.
         *
         * @param context Application context to avoid memory leaks.
         * @return AppDatabase instance
         */
        fun getDatabase(context: Context): AppDatabase {
            // Check if instance already exists; if not, synchronize to create it
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext, // Use application context to prevent leaks
                    AppDatabase::class.java,    // Database class
                    "wifi_signal_database"      // Name of the database file
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
