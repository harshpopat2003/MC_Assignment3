package com.example.wifisignallogger.data.repository

import androidx.lifecycle.LiveData
import com.example.wifisignallogger.data.database.LocationDao
import com.example.wifisignallogger.data.database.WifiReadingDao
import com.example.wifisignallogger.data.model.Location
import com.example.wifisignallogger.data.model.LocationWithReadings
import com.example.wifisignallogger.data.model.WifiReading

// Repository class managing data operations for Locations and WifiReadings
class WifiRepository(
    private val locationDao: LocationDao, // DAO for Location entity
    private val wifiReadingDao: WifiReadingDao // DAO for WifiReading entity
) {

    val allLocations: LiveData<List<Location>> = locationDao.getAllLocations() // All Locations as LiveData
    val allLocationsWithReadings: LiveData<List<LocationWithReadings>> = locationDao.getAllLocationsWithReadings() // All Locations with their WifiReadings

    suspend fun insertLocation(name: String): Long { // Insert a new Location
        val location = Location(name = name)
        return locationDao.insert(location)
    }

    suspend fun saveWifiReadings(locationId: Long, readings: List<WifiReading>) { // Save a list of WifiReadings
        wifiReadingDao.insertAll(readings)
    }

    fun getLocationWithReadings(locationId: Long): LiveData<LocationWithReadings> { // Get a Location with its WifiReadings
        return locationDao.getLocationWithReadings(locationId)
    }

    suspend fun getReadingsForLocation(locationId: Long): List<WifiReading> { // Get all WifiReadings for a specific Location
        return wifiReadingDao.getReadingsForLocation(locationId)
    }

    suspend fun getLocationCount(): Int { // Get total number of Locations
        return locationDao.getCount()
    }

    suspend fun getRssiStatsForLocation(locationId: Long): Triple<Int?, Int?, Double?> { // Get min, max, and avg RSSI for a Location
        val min = wifiReadingDao.getMinRssiForLocation(locationId)
        val max = wifiReadingDao.getMaxRssiForLocation(locationId)
        val avg = wifiReadingDao.getAvgRssiForLocation(locationId)
        return Triple(min, max, avg)
    }
}
