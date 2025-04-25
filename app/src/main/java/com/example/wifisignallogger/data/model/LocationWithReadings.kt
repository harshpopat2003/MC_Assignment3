package com.example.wifisignallogger.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class LocationWithReadings(
    @Embedded val location: Location,
    @Relation(
        parentColumn = "id",
        entityColumn = "locationId"
    )
    val readings: List<WifiReading>
)