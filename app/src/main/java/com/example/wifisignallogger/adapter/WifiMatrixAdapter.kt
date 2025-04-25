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

class WifiMatrixAdapter : RecyclerView.Adapter<WifiMatrixAdapter.MatrixCellViewHolder>() {

    private var wifiReadings: List<WifiReading> = emptyList()
    private var cellCount: Int = 100 // Default to 100 cells

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatrixCellViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wifi_matrix_cell, parent, false)
        return MatrixCellViewHolder(view)
    }

    override fun onBindViewHolder(holder: MatrixCellViewHolder, position: Int) {
        val reading = wifiReadings.find { it.index == position }
        holder.bind(reading, position)
    }

    override fun getItemCount(): Int = cellCount

    fun setData(readings: List<WifiReading>, count: Int = 100) {
        wifiReadings = readings
        cellCount = count
        notifyDataSetChanged()
    }

    inner class MatrixCellViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardCell: CardView = itemView.findViewById(R.id.cardCell)
        private val textRssi: TextView = itemView.findViewById(R.id.textRssi)
        private val textSsid: TextView = itemView.findViewById(R.id.textSsid)

        fun bind(reading: WifiReading?, position: Int) {
            if (reading != null) {
                val rssi = reading.rssi

                // Set text and color based on RSSI
                textRssi.text = rssi.toString()
                textSsid.text = reading.ssid.take(10) // Show first 10 chars of SSID

                // Set background color based on signal strength
                cardCell.setCardBackgroundColor(WifiScanner.mapRssiToColor(rssi))

                // Make text more visible based on background color
                val textColor = if (WifiScanner.mapRssiToStrength(rssi) > 50) {
                    android.graphics.Color.BLACK
                } else {
                    android.graphics.Color.WHITE
                }
                textRssi.setTextColor(textColor)
                textSsid.setTextColor(textColor)

            } else {
                // No reading for this cell
                textRssi.text = "N/A"
                textSsid.text = ""
                cardCell.setCardBackgroundColor(android.graphics.Color.LTGRAY)
                textRssi.setTextColor(android.graphics.Color.BLACK)
            }
        }
    }
}