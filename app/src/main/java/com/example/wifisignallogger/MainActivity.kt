package com.example.wifisignallogger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wifisignallogger.adapter.LocationAdapter
import com.example.wifisignallogger.data.database.AppDatabase
import com.example.wifisignallogger.data.model.Location
import com.example.wifisignallogger.data.repository.WifiRepository

class MainActivity : AppCompatActivity() {

    // Adapter for displaying location list in RecyclerView
    private lateinit var locationAdapter: LocationAdapter
    // Repository for accessing the wifi data from database
    private lateinit var wifiRepository: WifiRepository

    // Permission request launcher to handle permission requests (like ACCESS_FINE_LOCATION)
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            if (locationGranted) {
                // If location permission is granted, nothing to do here
            } else {
                // If permission is denied, show a toast with an error message
                Toast.makeText(
                    this,
                    R.string.error_permission_denied,
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request necessary permissions
        requestPermissions()

        // Initialize the database and the repository
        val database = AppDatabase.getDatabase(this)
        wifiRepository = WifiRepository(database.locationDao(), database.wifiReadingDao())

        // Setup RecyclerView to display the list of locations
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLocations)
        recyclerView.layoutManager = LinearLayoutManager(this)
        locationAdapter = LocationAdapter { location ->
            // Handle item click - navigate to ScanActivity with location details
            val intent = Intent(this, ScanActivity::class.java)
            intent.putExtra("locationId", location.id)
            startActivity(intent)
        }
        recyclerView.adapter = locationAdapter

        // Observe the list of locations from the database and update the adapter
        wifiRepository.allLocations.observe(this, Observer { locations ->
            locationAdapter.submitList(locations)
        })

        // Button to start a new scan
        val buttonNewScan = findViewById<Button>(R.id.buttonNewScan)
        buttonNewScan.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        // Button to compare the locations, but requires at least 3 locations
        val buttonCompare = findViewById<Button>(R.id.buttonCompare)
        buttonCompare.setOnClickListener {
            // Check if there are at least 3 locations for comparison
            wifiRepository.allLocationsWithReadings.observe(this, Observer { locations ->
                if (locations.size >= 3) {
                    // Start the ComparisonActivity if there are 3 or more locations
                    startActivity(Intent(this, ComparisonActivity::class.java))
                } else {
                    // Show a toast message if there are fewer than 3 locations
                    Toast.makeText(
                        this,
                        "Need at least 3 locations to compare. You have ${locations.size}.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }

    // Function to request necessary permissions for location and Wi-Fi access
    private fun requestPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )

        // For Android 12 and above, add BLUETOOTH_SCAN permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        // Check which permissions are missing
        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        // If any permissions are missing, request them
        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        } else {
            // Log a message if all necessary permissions are already granted
            Log.d("MainActivity", "All required permissions are already granted.")
        }
    }
}
