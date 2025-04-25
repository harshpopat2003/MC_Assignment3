package com.example.wifisignallogger

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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

    private lateinit var locationAdapter: LocationAdapter
    private lateinit var wifiRepository: WifiRepository

    // Permission request launcher
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
            if (locationGranted) {
                // Permission granted
            } else {
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

        // Check and request permissions
        requestPermissions()

        // Initialize database and repository
        val database = AppDatabase.getDatabase(this)
        wifiRepository = WifiRepository(database.locationDao(), database.wifiReadingDao())

        // Setup RecyclerView
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewLocations)
        recyclerView.layoutManager = LinearLayoutManager(this)
        locationAdapter = LocationAdapter { location ->
            // Handle location item click
            val intent = Intent(this, ScanActivity::class.java)
            intent.putExtra("locationId", location.id)
            startActivity(intent)
        }
        recyclerView.adapter = locationAdapter

        // Observe location changes
        wifiRepository.allLocations.observe(this, Observer { locations ->
            locationAdapter.submitList(locations)
        })

        // Setup buttons
        val buttonNewScan = findViewById<Button>(R.id.buttonNewScan)
        buttonNewScan.setOnClickListener {
            val intent = Intent(this, ScanActivity::class.java)
            startActivity(intent)
        }

        val buttonCompare = findViewById<Button>(R.id.buttonCompare)
        buttonCompare.setOnClickListener {
            // Check if we have at least 3 locations before allowing comparison
            wifiRepository.allLocationsWithReadings.observe(this, Observer { locations ->
                if (locations.size >= 3) {
                    startActivity(Intent(this, ComparisonActivity::class.java))
                } else {
                    Toast.makeText(
                        this,
                        "Need at least 3 locations to compare. You have ${locations.size}.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }

    private fun requestPermissions() {
        val requiredPermissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE
        )

        // For Android 12+, add BLUETOOTH_SCAN permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
        }

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest)
        }
    }
}