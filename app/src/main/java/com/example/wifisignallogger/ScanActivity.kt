package com.example.wifisignallogger

import android.net.wifi.ScanResult
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wifisignallogger.adapter.WifiMatrixAdapter
import com.example.wifisignallogger.data.database.AppDatabase
import com.example.wifisignallogger.data.model.WifiReading
import com.example.wifisignallogger.data.repository.WifiRepository
import com.example.wifisignallogger.util.WifiScanner
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ScanActivity : AppCompatActivity() {

    // WiFi scanner instance for scanning Wi-Fi networks
    private lateinit var wifiScanner: WifiScanner
    // Repository for accessing Wi-Fi data from the database
    private lateinit var wifiRepository: WifiRepository
    // Adapter for displaying Wi-Fi readings in a grid
    private lateinit var matrixAdapter: WifiMatrixAdapter

    // Views for user interface elements
    private lateinit var editTextLocationName: TextInputEditText
    private lateinit var buttonStartScan: Button
    private lateinit var textViewScanStatus: TextView
    private lateinit var progressBarScanning: ProgressBar
    private lateinit var recyclerViewWifiMatrix: RecyclerView

    // Variable to hold the existing location ID if we're viewing an existing location
    private var locationId: Long = -1
    // Flag to determine if we are in view mode or scanning new data
    private var isViewMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        // Initialize the UI views
        editTextLocationName = findViewById(R.id.editTextLocationName)
        buttonStartScan = findViewById(R.id.buttonStartScan)
        textViewScanStatus = findViewById(R.id.textViewScanStatus)
        progressBarScanning = findViewById(R.id.progressBarScanning)
        recyclerViewWifiMatrix = findViewById(R.id.recyclerViewWifiMatrix)

        // Initialize Wi-Fi scanner and repository
        wifiScanner = WifiScanner(this)
        val database = AppDatabase.getDatabase(this)
        wifiRepository = WifiRepository(database.locationDao(), database.wifiReadingDao())

        // Set up the RecyclerView to display Wi-Fi readings in a 10x10 grid
        matrixAdapter = WifiMatrixAdapter()
        recyclerViewWifiMatrix.layoutManager = GridLayoutManager(this, 10) // 10x10 grid
        recyclerViewWifiMatrix.adapter = matrixAdapter

        // Check if we are viewing an existing location (passed via Intent)
        locationId = intent.getLongExtra("locationId", -1)
        if (locationId != -1L) {
            isViewMode = true
            loadExistingLocation()
        }

        // Button to start a new scan or to go back if in view mode
        buttonStartScan.setOnClickListener {
            if (!isViewMode) {
                startScan()  // Start scanning for Wi-Fi networks
            } else {
                finish()  // Exit to the previous activity (MainActivity)
            }
        }
    }

    // Load data for an existing location
    private fun loadExistingLocation() {
        // Update UI for view mode
        buttonStartScan.text = "Back"
        editTextLocationName.isEnabled = false

        // Load the location data from the repository and update UI
        lifecycleScope.launch {
            wifiRepository.getLocationWithReadings(locationId).observe(this@ScanActivity) { locationWithReadings ->
                if (locationWithReadings != null) {
                    // Set the location name in the edit text
                    editTextLocationName.setText(locationWithReadings.location.name)

                    // Update status text
                    textViewScanStatus.text = "Showing data for ${locationWithReadings.location.name}"

                    // Update the RecyclerView with the readings
                    matrixAdapter.setData(locationWithReadings.readings)
                }
            }
        }
    }

    // Start scanning for Wi-Fi networks and saving data
    private fun startScan() {
        val locationName = editTextLocationName.text.toString().trim()

        // Validate location name input
        if (locationName.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_location, Toast.LENGTH_SHORT).show()
            return
        }

        // Disable input during the scan to prevent changes
        editTextLocationName.isEnabled = false
        buttonStartScan.isEnabled = false

        // Show progress bar and update status
        textViewScanStatus.text = getString(R.string.scanning_in_progress)
        progressBarScanning.visibility = View.VISIBLE
        progressBarScanning.progress = 0

        lifecycleScope.launch {
            try {
                // Create a new location in the database
                val newLocationId = wifiRepository.insertLocation(locationName)

                // Register the Wi-Fi scanner to start scanning
                wifiScanner.register()

                // List to hold all the Wi-Fi scan results
                val allReadings = mutableListOf<ScanResult>()
                while (allReadings.size < 100) {
                    // Start a Wi-Fi scan
                    wifiScanner.startScan()

                    // Wait for scan results
                    delay(1000)

                    // Get the current scan results
                    val currentResults = wifiScanner.getCurrentScanResults()

                    // Log scan iteration results for debugging
                    Log.d("ScanActivity", "Scan iteration returned ${currentResults.size} results, total readings: ${allReadings.size}")

                    // Add current scan results to the list
                    allReadings.addAll(currentResults)

                    // Update the progress bar based on the number of results
                    withContext(Dispatchers.Main) {
                        progressBarScanning.progress = allReadings.size.coerceAtMost(100)
                    }
                }

                // If no access points were detected, show a warning
                if (allReadings.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ScanActivity, "No WiFi access points detected.", Toast.LENGTH_LONG).show()
                    }
                }

                // Process and save the scan results in the database
                val wifiReadings = processReadings(allReadings, newLocationId)
                wifiRepository.saveWifiReadings(newLocationId, wifiReadings)

                // Update UI with scan completion status
                withContext(Dispatchers.Main) {
                    textViewScanStatus.text = getString(R.string.scan_complete)
                    progressBarScanning.visibility = View.GONE
                    buttonStartScan.isEnabled = true
                    buttonStartScan.text = "Done"
                    buttonStartScan.setOnClickListener { finish() }

                    // Display the Wi-Fi readings in the grid
                    matrixAdapter.setData(wifiReadings)
                }

            } catch (e: Exception) {
                // Handle any errors that occur during the scan
                withContext(Dispatchers.Main) {
                    textViewScanStatus.text = "Error: ${e.message}"
                    progressBarScanning.visibility = View.GONE
                    buttonStartScan.isEnabled = true
                    editTextLocationName.isEnabled = true
                }
            } finally {
                // Unregister the Wi-Fi scanner when done
                wifiScanner.unregister()
            }
        }
    }

    // Process the raw scan results and convert them to WifiReading objects
    private fun processReadings(scanResults: List<ScanResult>, locationId: Long): List<WifiReading> {
        // Take the first 100 scan results and map them to WifiReading objects
        return scanResults.take(100).mapIndexed { index, scanResult ->
            WifiReading(
                locationId = locationId,
                ssid = scanResult.SSID.ifEmpty { "<Hidden>" },
                bssid = scanResult.BSSID,
                rssi = scanResult.level,
                frequency = scanResult.frequency,
                index = index
            )
        }
    }

    // Cleanup the Wi-Fi scanner when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        wifiScanner.unregister()
    }
}
