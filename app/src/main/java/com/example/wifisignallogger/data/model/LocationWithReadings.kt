package com.example.wifisignallogger.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Data class representing a Location along with its associated WiFi readings.
 * This is used for Room's @Relation query to fetch related data in one object.
 */
data class LocationWithReadings(

    /**
     * The Location entity (parent table).
     * Fetched directly and embedded into this object.
     */
    @Embedded val location: Location,

    /**
     * List of WifiReading entities associated with the Location.
     * Relation is established by matching Location's ID with WifiReading's locationId.
     */
    @Relation(
        parentColumn = "id",          // Column in the parent entity (Location)
        entityColumn = "locationId"   // Corresponding column in the child entity (WifiReading)
    )
    val readings: List<WifiReading>
)
