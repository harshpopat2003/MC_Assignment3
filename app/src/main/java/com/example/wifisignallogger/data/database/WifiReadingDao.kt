package com.example.wifisignallogger.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.wifisignallogger.data.model.WifiReading

@Dao
interface WifiReadingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<WifiReading>)

    @Query("SELECT * FROM wifi_readings WHERE locationId = :locationId ORDER BY `index`")
    suspend fun getReadingsForLocation(locationId: Long): List<WifiReading>

    @Query("SELECT MIN(rssi) FROM wifi_readings WHERE locationId = :locationId")
    suspend fun getMinRssiForLocation(locationId: Long): Int?

    @Query("SELECT MAX(rssi) FROM wifi_readings WHERE locationId = :locationId")
    suspend fun getMaxRssiForLocation(locationId: Long): Int?

    @Query("SELECT AVG(rssi) FROM wifi_readings WHERE locationId = :locationId")
    suspend fun getAvgRssiForLocation(locationId: Long): Double?
}