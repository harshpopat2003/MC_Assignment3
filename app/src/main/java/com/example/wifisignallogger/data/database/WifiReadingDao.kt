package com.example.wifisignallogger.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wifisignallogger.data.model.WifiReading

/**
 * Data Access Object (DAO) interface for WifiReading entity.
 * Defines database operations related to Wifi signal readings.
 */
@Dao
interface WifiReadingDao {

    /**
     * Inserts a list of WifiReading objects into the database.
     * If a conflict occurs (same primary key), replaces the old entry.
     * @param readings List of WifiReading objects to insert.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<WifiReading>)

    /**
     * Retrieves all WifiReadings associated with a specific Location,
     * ordered by the index field (typically representing grid position).
     * @param locationId ID of the Location.
     * @return List of WifiReading objects.
     */
    @Query("SELECT * FROM wifi_readings WHERE locationId = :locationId ORDER BY `index`")
    suspend fun getReadingsForLocation(locationId: Long): List<WifiReading>

    /**
     * Retrieves the minimum RSSI (signal strength) value for a given Location.
     * @param locationId ID of the Location.
     * @return Minimum RSSI value, or null if no readings exist.
     */
    @Query("SELECT MIN(rssi) FROM wifi_readings WHERE locationId = :locationId")
    suspend fun getMinRssiForLocation(locationId: Long): Int?

    /**
     * Retrieves the maximum RSSI (signal strength) value for a given Location.
     * @param locationId ID of the Location.
     * @return Maximum RSSI value, or null if no readings exist.
     */
    @Query("SELECT MAX(rssi) FROM wifi_readings WHERE locationId = :locationId")
    suspend fun getMaxRssiForLocation(locationId: Long): Int?

    /**
     * Retrieves the average RSSI (signal strength) value for a given Location.
     * @param locationId ID of the Location.
     * @return Average RSSI value, or null if no readings exist.
     */
    @Query("SELECT AVG(rssi) FROM wifi_readings WHERE locationId = :locationId")
    suspend fun getAvgRssiForLocation(locationId: Long): Double?
}
