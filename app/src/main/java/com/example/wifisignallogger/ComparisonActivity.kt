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

    private lateinit var wifiRepository: WifiRepository
    private lateinit var tableLayoutStats: TableLayout
    private lateinit var recyclerViewMatrix1: RecyclerView
    private lateinit var recyclerViewMatrix2: RecyclerView
    private lateinit var recyclerViewMatrix3: RecyclerView
    private lateinit var textViewLocation1Name: TextView
    private lateinit var textViewLocation2Name: TextView
    private lateinit var textViewLocation3Name: TextView

    private lateinit var matrixAdapter1: WifiMatrixAdapter
    private lateinit var matrixAdapter2: WifiMatrixAdapter
    private lateinit var matrixAdapter3: WifiMatrixAdapter

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
        val database = AppDatabase.getDatabase(this)
        wifiRepository = WifiRepository(database.locationDao(), database.wifiReadingDao())

        // Setup RecyclerViews
        setupMatrixRecyclerViews()

        // Load and display data
        loadLocationsData()
    }

    private fun setupMatrixRecyclerViews() {
        // Matrix 1
        matrixAdapter1 = WifiMatrixAdapter()
        recyclerViewMatrix1.layoutManager = GridLayoutManager(this, 10) // 10x10 grid
        recyclerViewMatrix1.adapter = matrixAdapter1

        // Matrix 2
        matrixAdapter2 = WifiMatrixAdapter()
        recyclerViewMatrix2.layoutManager = GridLayoutManager(this, 10) // 10x10 grid
        recyclerViewMatrix2.adapter = matrixAdapter2

        // Matrix 3
        matrixAdapter3 = WifiMatrixAdapter()
        recyclerViewMatrix3.layoutManager = GridLayoutManager(this, 10) // 10x10 grid
        recyclerViewMatrix3.adapter = matrixAdapter3
    }

    private fun loadLocationsData() {
        wifiRepository.allLocationsWithReadings.observe(this, Observer { locationsWithReadings ->
            if (locationsWithReadings.size >= 3) {
                // Get the 3 most recent locations
                val topLocations = locationsWithReadings.sortedByDescending { it.location.timestamp }.take(3)

                // Display location data
                displayLocationData(topLocations)

                // Calculate and display statistics
                calculateStatistics(topLocations)
            }
        })
    }

    private fun displayLocationData(locations: List<LocationWithReadings>) {
        if (locations.size >= 3) {
            // Location 1 (most recent)
            val location1 = locations[0]
            textViewLocation1Name.text = location1.location.name
            matrixAdapter1.setData(location1.readings)

            // Location 2
            val location2 = locations[1]
            textViewLocation2Name.text = location2.location.name
            matrixAdapter2.setData(location2.readings)

            // Location 3
            val location3 = locations[2]
            textViewLocation3Name.text = location3.location.name
            matrixAdapter3.setData(location3.readings)
        }
    }

    private fun calculateStatistics(locations: List<LocationWithReadings>) {
        lifecycleScope.launch {
            // Clear existing rows (except header)
            while (tableLayoutStats.childCount > 1) {
                tableLayoutStats.removeViewAt(1)
            }

            // Add stats for each location
            for (locationWithReadings in locations) {
                val location = locationWithReadings.location
                val readings = locationWithReadings.readings

                // Calculate stats
                val minRssi = readings.minOfOrNull { it.rssi } ?: 0
                val maxRssi = readings.maxOfOrNull { it.rssi } ?: 0
                val avgRssi = readings.map { it.rssi }.average()
                val range = (maxRssi - minRssi).absoluteValue

                // Create row
                val tableRow = TableRow(this@ComparisonActivity)
                tableRow.layoutParams = TableLayout.LayoutParams(
                    TableLayout.LayoutParams.MATCH_PARENT,
                    TableLayout.LayoutParams.WRAP_CONTENT
                )
                tableRow.setPadding(4, 4, 4, 4)

                // Add cells
                addTextCell(tableRow, location.name)
                addTextCell(tableRow, minRssi.toString())
                addTextCell(tableRow, maxRssi.toString())
                addTextCell(tableRow, String.format("%.1f", avgRssi))
                addTextCell(tableRow, range.toString())

                // Add row to table
                tableLayoutStats.addView(tableRow)
            }
        }
    }

    private fun addTextCell(row: TableRow, text: String) {
        val textView = TextView(this)
        textView.text = text
        textView.setPadding(8, 8, 8, 8)
        textView.gravity = Gravity.CENTER_VERTICAL

        // Set layout parameters
        val layoutParams = TableRow.LayoutParams(
            0, // Width - 0 means it will be weighted
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.weight = 1f
        layoutParams.setMargins(2, 2, 2, 2)
        textView.layoutParams = layoutParams

        // Add to row
        row.addView(textView)
    }
}