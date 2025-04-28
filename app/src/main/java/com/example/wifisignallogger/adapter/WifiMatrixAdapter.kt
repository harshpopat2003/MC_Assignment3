package com.example.wifisignallogger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.wifisignallogger.R
import com.example.wifisignallogger.data.model.WifiReading
import com.example.wifisignallogger.util.WifiScanner

/**
 * Adapter class for displaying a matrix grid of WiFi readings in a RecyclerView.
 * Each cell represents a WiFi reading (RSSI and SSID) or an empty slot if no reading.
 */
class WifiMatrixAdapter : RecyclerView.Adapter<WifiMatrixAdapter.MatrixCellViewHolder>() {

    private var wifiReadings: List<WifiReading> = emptyList() // List of WifiReading data
    private var cellCount: Int = 100 // Total number of cells (default to 100)

    // Called when RecyclerView needs a new ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatrixCellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wifi_matrix_cell, parent, false)
        return MatrixCellViewHolder(view)
    }

    // Binds data to each ViewHolder at a given position
    override fun onBindViewHolder(holder: MatrixCellViewHolder, position: Int) {
        val reading = wifiReadings.find { it.index == position } // Find reading for this cell
        holder.bind(reading, position)
    }

    // Returns total number of cells
    override fun getItemCount(): Int = cellCount

    /**
     * Updates the adapter's data and refreshes the RecyclerView.
     * @param readings List of WifiReading objects.
     * @param count Total number of cells to display (default 100).
     */
    fun setData(readings: List<WifiReading>, count: Int = 100) {
        wifiReadings = readings
        cellCount = count
        notifyDataSetChanged()
    }

    /**
     * ViewHolder class for each matrix cell showing WiFi information.
     */
    inner class MatrixCellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardCell: CardView = itemView.findViewById(R.id.cardCell)
        private val textRssi: TextView = itemView.findViewById(R.id.textRssi)
        private val textSsid: TextView = itemView.findViewById(R.id.textSsid)

        /**
         * Binds a WifiReading object to the cell views.
         * @param reading WifiReading data for the cell (null if no data).
         * @param position Position of the cell.
         */
        fun bind(reading: WifiReading?, position: Int) {
            if (reading != null) {
                val rssi = reading.rssi

                // Set RSSI value and truncated SSID text
                textRssi.text = rssi.toString()
                textSsid.text = reading.ssid.take(10) // Show only first 10 characters of SSID

                // Set background color based on signal strength
                cardCell.setCardBackgroundColor(WifiScanner.mapRssiToColor(rssi))

                // Adjust text color for better visibility based on signal strength
                val textColor = if (WifiScanner.mapRssiToStrength(rssi) > 50) {
                    android.graphics.Color.BLACK
                } else {
                    android.graphics.Color.WHITE
                }
                textRssi.setTextColor(textColor)
                textSsid.setTextColor(textColor)

            } else {
                // No reading available for this position
                textRssi.text = "N/A"
                textSsid.text = ""
                cardCell.setCardBackgroundColor(android.graphics.Color.LTGRAY)
                textRssi.setTextColor(android.graphics.Color.BLACK)
            }
        }
    }
}
