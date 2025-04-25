// WifiRepository.kt
package com.example.wifisignallogger.data.repository

import androidx.lifecycle.LiveData
import com.example.wifisignallogger.data.database.LocationDao
import com.example.wifisignallogger.data.database.WifiReadingDao
import com.example.wifisignallogger.data.model.Location
import com.example.wifisignallogger.data.model.LocationWithReadings
import com.example.wifisignallogger.data.model.WifiReading

class WifiRepository(
    private val locationDao: LocationDao,
    private val wifiReadingDao: WifiReadingDao
) {

    val allLocations: LiveData<List<Location>> = locationDao.getAllLocations()
    val allLocationsWithReadings: LiveData<List<LocationWithReadings>> = locationDao.getAllLocationsWithReadings()

    suspend fun insertLocation(name: String): Long {
        val location = Location(name = name)
        return locationDao.insert(location)
    }

    suspend fun saveWifiReadings(locationId: Long, readings: List<WifiReading>) {
        wifiReadingDao.insertAll(readings)
    }

    fun getLocationWithReadings(locationId: Long): LiveData<LocationWithReadings> {
        return locationDao.getLocationWithReadings(locationId)
    }

    suspend fun getReadingsForLocation(locationId: Long): List<WifiReading> {
        return wifiReadingDao.getReadingsForLocation(locationId)
    }

    suspend fun getLocationCount(): Int {
        return locationDao.getCount()
    }

    suspend fun getRssiStatsForLocation(locationId: Long): Triple<Int?, Int?, Double?> {
        val min = wifiReadingDao.getMinRssiForLocation(locationId)
        val max = wifiReadingDao.getMaxRssiForLocation(locationId)
        val avg = wifiReadingDao.getAvgRssiForLocation(locationId)
        return Triple(min, max, avg)
    }
}