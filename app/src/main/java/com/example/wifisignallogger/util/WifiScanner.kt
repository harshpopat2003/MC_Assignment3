// WifiScanner.kt
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

class WifiScanner(private val context: Context) {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val scanResultsChannel = Channel<List<ScanResult>>(Channel.CONFLATED)
    private var registered = false

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
            if (success) {
                val results = wifiManager.scanResults
                scanResultsChannel.trySend(results)
            } else {
                scanResultsChannel.trySend(emptyList())
            }
        }
    }

    init {
        // Enable WiFi if it's not enabled
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }
    }

    fun register() {
        if (!registered) {
            val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            context.registerReceiver(wifiScanReceiver, intentFilter)
            registered = true
        }
    }

    fun unregister() {
        if (registered) {
            try {
                context.unregisterReceiver(wifiScanReceiver)
            } catch (e: Exception) {
                // Receiver not registered
            }
            registered = false
        }
    }

    fun startScan(): Boolean {
        return wifiManager.startScan()
    }

    fun getScanResultsFlow(): Flow<List<ScanResult>> {
        return scanResultsChannel.receiveAsFlow()
    }

    fun getCurrentScanResults(): List<ScanResult> {
        return wifiManager.scanResults
    }

    companion object {
        // RSSI ranges from -100 dBm (weak) to about -30 dBm (strong)
        const val MIN_RSSI = -100
        const val MAX_RSSI = -30

        // Map RSSI to a 0-100 scale for visualization
        fun mapRssiToStrength(rssi: Int): Int {
            return ((rssi - MIN_RSSI) * 100.0 / (MAX_RSSI - MIN_RSSI)).toInt().coerceIn(0, 100)
        }

        // Map RSSI to a color (red = weak, green = strong)
        fun mapRssiToColor(rssi: Int): Int {
            val normalizedStrength = mapRssiToStrength(rssi).toFloat() / 100f

            // Red component (for weak signal)
            val r = ((1f - normalizedStrength) * 255).toInt()

            // Green component (for strong signal)
            val g = (normalizedStrength * 255).toInt()

            // Blue component (fixed at 0)
            val b = 0

            return android.graphics.Color.rgb(r, g, b)
        }
    }
}