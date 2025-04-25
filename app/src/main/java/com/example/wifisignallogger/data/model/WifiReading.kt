// WifiReading.kt
package com.example.wifisignallogger.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "wifi_readings",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("locationId")]
)
data class WifiReading(
    @androidx.room.PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val locationId: Long,
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val index: Int // Position in the matrix (0-99)
)