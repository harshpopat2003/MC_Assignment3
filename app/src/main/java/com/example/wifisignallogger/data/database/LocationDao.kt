package com.example.wifisignallogger.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.wifisignallogger.data.model.Location
import com.example.wifisignallogger.data.model.LocationWithReadings

@Dao
interface LocationDao {

    @Insert
    suspend fun insert(location: Location): Long

    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocations(): LiveData<List<Location>>

    @Transaction
    @Query("SELECT * FROM locations WHERE id = :locationId")
    fun getLocationWithReadings(locationId: Long): LiveData<LocationWithReadings>

    @Transaction
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocationsWithReadings(): LiveData<List<LocationWithReadings>>

    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getCount(): Int
}
