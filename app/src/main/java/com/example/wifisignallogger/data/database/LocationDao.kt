package com.example.wifisignallogger.data.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.wifisignallogger.data.model.Location
import com.example.wifisignallogger.data.model.LocationWithReadings

/**
 * Data Access Object (DAO) interface for the Location entity.
 * Defines database operations related to Locations and their associated WiFi readings.
 */
@Dao
interface LocationDao {

    /**
     * Inserts a new Location into the database.
     * @param location Location object to insert.
     * @return ID (primary key) of the inserted Location.
     */
    @Insert
    suspend fun insert(location: Location): Long

    /**
     * Retrieves all Location entries ordered by their timestamp (most recent first).
     * @return LiveData list of all Locations.
     */
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocations(): LiveData<List<Location>>

    /**
     * Retrieves a specific Location along with its associated WifiReadings.
     * This uses a Room @Transaction to ensure atomic loading of related data.
     * @param locationId ID of the Location.
     * @return LiveData of LocationWithReadings object.
     */
    @Transaction
    @Query("SELECT * FROM locations WHERE id = :locationId")
    fun getLocationWithReadings(locationId: Long): LiveData<LocationWithReadings>

    /**
     * Retrieves all Locations along with their associated WifiReadings,
     * ordered by timestamp (most recent first).
     * @return LiveData list of LocationWithReadings.
     */
    @Transaction
    @Query("SELECT * FROM locations ORDER BY timestamp DESC")
    fun getAllLocationsWithReadings(): LiveData<List<LocationWithReadings>>

    /**
     * Returns the total number of Locations stored in the database.
     * @return Count of Location entries.
     */
    @Query("SELECT COUNT(*) FROM locations")
    suspend fun getCount(): Int
}
