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

    private lateinit var wifiScanner: WifiScanner
    private lateinit var wifiRepository: WifiRepository
    private lateinit var matrixAdapter: WifiMatrixAdapter

    private lateinit var editTextLocationName: TextInputEditText
    private lateinit var buttonStartScan: Button
    private lateinit var textViewScanStatus: TextView
    private lateinit var progressBarScanning: ProgressBar
    private lateinit var recyclerViewWifiMatrix: RecyclerView

    // Existing location ID if we're viewing an existing location
    private var locationId: Long = -1
    private var isViewMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        // Initialize views
        editTextLocationName = findViewById(R.id.editTextLocationName)
        buttonStartScan = findViewById(R.id.buttonStartScan)
        textViewScanStatus = findViewById(R.id.textViewScanStatus)
        progressBarScanning = findViewById(R.id.progressBarScanning)
        recyclerViewWifiMatrix = findViewById(R.id.recyclerViewWifiMatrix)

        // Initialize WiFi scanner
        wifiScanner = WifiScanner(this)

        // Initialize database and repository
        val database = AppDatabase.getDatabase(this)
        wifiRepository = WifiRepository(database.locationDao(), database.wifiReadingDao())

        // Setup RecyclerView
        matrixAdapter = WifiMatrixAdapter()
        recyclerViewWifiMatrix.layoutManager = GridLayoutManager(this, 10) // 10x10 grid
        recyclerViewWifiMatrix.adapter = matrixAdapter

        // Check if we're viewing an existing location
        locationId = intent.getLongExtra("locationId", -1)
        if (locationId != -1L) {
            isViewMode = true
            loadExistingLocation()
        }

        // Setup button click listener
        buttonStartScan.setOnClickListener {
            if (!isViewMode) {
                startScan()
            } else {
                // In view mode, return to main activity
                finish()
            }
        }
    }

    private fun loadExistingLocation() {
        // Update UI for view mode
        buttonStartScan.text = "Back"
        editTextLocationName.isEnabled = false

        // Load location data
        lifecycleScope.launch {
            wifiRepository.getLocationWithReadings(locationId).observe(this@ScanActivity) { locationWithReadings ->
                if (locationWithReadings != null) {
                    // Set location name
                    editTextLocationName.setText(locationWithReadings.location.name)

                    // Update status
                    textViewScanStatus.text = "Showing data for ${locationWithReadings.location.name}"

                    // Display readings in matrix
                    matrixAdapter.setData(locationWithReadings.readings)
                }
            }
        }
    }

    private fun startScan() {
        val locationName = editTextLocationName.text.toString().trim()
        if (locationName.isEmpty()) {
            Toast.makeText(this, R.string.error_empty_location, Toast.LENGTH_SHORT).show()
            return
        }

        // Disable input during scan
        editTextLocationName.isEnabled = false
        buttonStartScan.isEnabled = false

        // Show progress
        textViewScanStatus.text = getString(R.string.scanning_in_progress)
        progressBarScanning.visibility = View.VISIBLE
        progressBarScanning.progress = 0

        lifecycleScope.launch {
            try {
                // Create location in database
                val newLocationId = wifiRepository.insertLocation(locationName)

                // Register WiFi scanner
                wifiScanner.register()

                // Collect readings until we have at least 100 RSSI values
                val allReadings = mutableListOf<ScanResult>()
                while (allReadings.size < 100) {
                    // Start a scan
                    wifiScanner.startScan()

                    // Wait for results
                    delay(1000)

                    // Get results
                    val currentResults = wifiScanner.getCurrentScanResults()

                    // Log each scan's result count
                    Log.d("ScanActivity", "Scan iteration returned ${currentResults.size} results, total readings: ${allReadings.size}")

                    allReadings.addAll(currentResults)

                    // Update progress
                    withContext(Dispatchers.Main) {
                        progressBarScanning.progress = allReadings.size.coerceAtMost(100)
                    }
                }

                // Warn the user if no access points were detected
                if (allReadings.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@ScanActivity, "No WiFi access points detected.", Toast.LENGTH_LONG).show()
                    }
                }

                // Process and save readings
                val wifiReadings = processReadings(allReadings, newLocationId)
                wifiRepository.saveWifiReadings(newLocationId, wifiReadings)

                // Update UI with results
                withContext(Dispatchers.Main) {
                    textViewScanStatus.text = getString(R.string.scan_complete)
                    progressBarScanning.visibility = View.GONE
                    buttonStartScan.isEnabled = true
                    buttonStartScan.text = "Done"
                    buttonStartScan.setOnClickListener { finish() }

                    // Display readings in matrix
                    matrixAdapter.setData(wifiReadings)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    textViewScanStatus.text = "Error: ${e.message}"
                    progressBarScanning.visibility = View.GONE
                    buttonStartScan.isEnabled = true
                    editTextLocationName.isEnabled = true
                }
            } finally {
                wifiScanner.unregister()
            }
        }
    }

    private fun processReadings(scanResults: List<ScanResult>, locationId: Long): List<WifiReading> {
        // Use the first 100 RSSI readings as-is
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

    override fun onDestroy() {
        super.onDestroy()
        wifiScanner.unregister()
    }
}
