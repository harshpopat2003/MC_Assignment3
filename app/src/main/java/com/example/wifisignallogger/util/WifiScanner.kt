package com.example.wifisignallogger.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

// Class to manage WiFi scanning and scan results
class WifiScanner(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager // WiFi manager system service
    private val scanResultsChannel = Channel<List<ScanResult>>(Channel.CONFLATED) // Channel for emitting scan results
    private var registered = false // Whether receiver is registered

    // BroadcastReceiver to handle WiFi scan results
    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false) // Check if scan was successful
            if (success) {
                val results = wifiManager.scanResults // Get scan results
                scanResultsChannel.trySend(results) // Send results to channel
            } else {
                scanResultsChannel.trySend(emptyList()) // Send empty list if failed
            }
        }
    }

    init {
        if (!wifiManager.isWifiEnabled) { // Enable WiFi if disabled
            wifiManager.isWifiEnabled = true
        }
    }

    fun register() { // Register the BroadcastReceiver
        if (!registered) {
            val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            context.registerReceiver(wifiScanReceiver, intentFilter)
            registered = true
        }
    }

    fun unregister() { // Unregister the BroadcastReceiver
        if (registered) {
            try {
                context.unregisterReceiver(wifiScanReceiver)
            } catch (e: Exception) {
                // Receiver not registered (ignore)
            }
            registered = false
        }
    }

    fun startScan(): Boolean { // Start a WiFi scan
        return wifiManager.startScan()
    }

    fun getScanResultsFlow(): Flow<List<ScanResult>> { // Expose scan results as a Flow
        return scanResultsChannel.receiveAsFlow()
    }

    fun getCurrentScanResults(): List<ScanResult> { // Get the current WiFi scan results directly
        return wifiManager.scanResults
    }

    companion object {
        const val MIN_RSSI = -100 // Minimum expected RSSI value (weak signal)
        const val MAX_RSSI = -30 // Maximum expected RSSI value (strong signal)

        fun mapRssiToStrength(rssi: Int): Int { // Normalize RSSI to 0–100 scale
            return ((rssi - MIN_RSSI) * 100.0 / (MAX_RSSI - MIN_RSSI)).toInt().coerceIn(0, 100)
        }

        fun mapRssiToColor(rssi: Int): Int { // Map RSSI strength to a color (red weak, green strong)
            val normalizedStrength = mapRssiToStrength(rssi).toFloat() / 100f
            val r = ((1f - normalizedStrength) * 255).toInt() // Red intensity (weaker signal = more red)
            val g = (normalizedStrength * 255).toInt() // Green intensity (stronger signal = more green)
            val b = 0 // Blue intensity fixed at 0
            return android.graphics.Color.rgb(r, g, b)
        }
    }
}
