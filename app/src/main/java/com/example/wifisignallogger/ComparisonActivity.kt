package com.example.wifisignallogger

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wifisignallogger.adapter.WifiMatrixAdapter
import com.example.wifisignallogger.data.database.AppDatabase
import com.example.wifisignallogger.data.model.Location
import com.example.wifisignallogger.data.model.LocationWithReadings
import com.example.wifisignallogger.data.repository.WifiRepository
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

class ComparisonActivity : AppCompatActivity() {

    private lateinit var wifiRepository: WifiRepository // Repository for accessing database
    private lateinit var tableLayoutStats: TableLayout // TableLayout for showing statistics
    private lateinit var recyclerViewMatrix1: RecyclerView // First matrix RecyclerView
    private lateinit var recyclerViewMatrix2: RecyclerView // Second matrix RecyclerView
    private lateinit var recyclerViewMatrix3: RecyclerView // Third matrix RecyclerView
    private lateinit var textViewLocation1Name: TextView // TextView for location 1 name
    private lateinit var textViewLocation2Name: TextView // TextView for location 2 name
    private lateinit var textViewLocation3Name: TextView // TextView for location 3 name

    private lateinit var matrixAdapter1: WifiMatrixAdapter // Adapter for matrix 1
    private lateinit var matrixAdapter2: WifiMatrixAdapter // Adapter for matrix 2
    private lateinit var matrixAdapter3: WifiMatrixAdapter // Adapter for matrix 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comparison)

        // Initialize views
        tableLayoutStats = findViewById(R.id.tableLayoutStats)
        recyclerViewMatrix1 = findViewById(R.id.recyclerViewMatrix1)
        recyclerViewMatrix2 = findViewById(R.id.recyclerViewMatrix2)
        recyclerViewMatrix3 = findViewById(R.id.recyclerViewMatrix3)
        textViewLocation1Name = findViewById(R.id.textViewLocation1Name)
        textViewLocation2Name = findViewById(R.id.textViewLocation2Name)
        textViewLocation3Name = findViewById(R.id.textViewLocation3Name)

        // Initialize database and repository
        val database = AppDatabase.getDatabase(this) // Get database instance
        wifiRepository = WifiRepository(database.locationDao(), database.wifiReadingDao())

        // Setup RecyclerViews
        setupMatrixRecyclerViews()

        // Load and display data
        loadLocationsData()
    }

    private fun setupMatrixRecyclerViews() {
        matrixAdapter1 = WifiMatrixAdapter() // Setup matrix 1
        recyclerViewMatrix1.layoutManager = GridLayoutManager(this, 10) // 10x10 grid
        recyclerViewMatrix1.adapter = matrixAdapter1

        matrixAdapter2 = WifiMatrixAdapter() // Setup matrix 2
        recyclerViewMatrix2.layoutManager = GridLayoutManager(this, 10)
        recyclerViewMatrix2.adapter = matrixAdapter2

        matrixAdapter3 = WifiMatrixAdapter() // Setup matrix 3
        recyclerViewMatrix3.layoutManager = GridLayoutManager(this, 10)
        recyclerViewMatrix3.adapter = matrixAdapter3
    }

    private fun loadLocationsData() {
        wifiRepository.allLocationsWithReadings.observe(this, Observer { locationsWithReadings ->
            if (locationsWithReadings.size >= 3) {
                val topLocations = locationsWithReadings.sortedByDescending { it.location.timestamp }.take(3) // Take 3 most recent locations
                displayLocationData(topLocations) // Display location names and matrices
                calculateStatistics(topLocations) // Calculate and display stats
            }
        })
    }

    private fun displayLocationData(locations: List<LocationWithReadings>) {
        if (locations.size >= 3) {
            val location1 = locations[0] // Most recent location
            textViewLocation1Name.text = location1.location.name
            matrixAdapter1.setData(location1.readings)

            val location2 = locations[1] // Second most recent
            textViewLocation2Name.text = location2.location.name
            matrixAdapter2.setData(location2.readings)

            val location3 = locations[2] // Third most recent
            textViewLocation3Name.text = location3.location.name
            matrixAdapter3.setData(location3.readings)
        }
    }

    private fun calculateStatistics(locations: List<LocationWithReadings>) {
        lifecycleScope.launch {
            while (tableLayoutStats.childCount > 1) { // Clear previous rows except header
                tableLayoutStats.removeViewAt(1)
            }

            for (locationWithReadings in locations) {
                val location = locationWithReadings.location
                val readings = locationWithReadings.readings

                val minRssi = readings.minOfOrNull { it.rssi } ?: 0 // Minimum RSSI
                val maxRssi = readings.maxOfOrNull { it.rssi } ?: 0 // Maximum RSSI
                val avgRssi = readings.map { it.rssi }.average() // Average RSSI
                val range = (maxRssi - minRssi).absoluteValue // Signal range

                val tableRow = TableRow(this@ComparisonActivity) // Create table row
                tableRow.layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                tableRow.setPadding(4, 4, 4, 4)

                addTextCell(tableRow, location.name) // Add location name
                addTextCell(tableRow, minRssi.toString()) // Add min RSSI
                addTextCell(tableRow, maxRssi.toString()) // Add max RSSI
                addTextCell(tableRow, String.format("%.1f", avgRssi)) // Add avg RSSI
                addTextCell(tableRow, range.toString()) // Add signal range

                tableLayoutStats.addView(tableRow) // Add row to table
            }
        }
    }

    private fun addTextCell(row: TableRow, text: String) {
        val textView = TextView(this) // Create TextView
        textView.text = text
        textView.setPadding(8, 8, 8, 8)
        textView.gravity = Gravity.CENTER_VERTICAL

        val layoutParams = TableRow.LayoutParams(
            0, // Width 0 for equal weight
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.weight = 1f // Equal weight for each cell
        layoutParams.setMargins(2, 2, 2, 2)
        textView.layoutParams = layoutParams

        row.addView(textView) // Add TextView to TableRow
    }
}
