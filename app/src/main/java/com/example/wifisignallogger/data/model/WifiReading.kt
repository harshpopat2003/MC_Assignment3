package com.example.wifisignallogger.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "wifi_readings", // Table name for WiFi readings
    foreignKeys = [
        ForeignKey(
            entity = Location::class, // Parent entity is Location
            parentColumns = ["id"], // Parent's primary key
            childColumns = ["locationId"], // Child's foreign key
            onDelete = ForeignKey.CASCADE // Delete readings if location is deleted
        )
    ],
    indices = [Index("locationId")] // Index on locationId for faster queries
)
data class WifiReading(
    @androidx.room.PrimaryKey(autoGenerate = true) // Primary key, auto-generated
    val id: Long = 0, // Unique ID for each WifiReading

    val locationId: Long, // Foreign key linking to Location

    val ssid: String, // SSID (WiFi network name)

    val bssid: String, // BSSID (MAC address of WiFi access point)

    val rssi: Int, // Received Signal Strength Indicator (signal strength in dBm)

    val frequency: Int, // Frequency of the WiFi signal (e.g., 2400 MHz, 5000 MHz)

    val index: Int // Position in the 10x10 matrix (0–99)
)
