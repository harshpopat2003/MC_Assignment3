package com.example.wifisignallogger.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.wifisignallogger.R
import com.example.wifisignallogger.data.model.Location
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adapter class for displaying a list of Location items in a RecyclerView.
 * @param onItemClick Callback function to handle item click events.
 */
class LocationAdapter(private val onItemClick: (Location) -> Unit) :
    ListAdapter<Location, LocationAdapter.LocationViewHolder>(LocationDiffCallback()) {

    // Called when RecyclerView needs a new ViewHolder of the given type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    // Binds data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = getItem(position)
        holder.bind(location)
    }

    /**
     * ViewHolder class to hold the views for each location item.
     */
    inner class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.textViewLocationName)
        private val timeTextView: TextView = itemView.findViewById(R.id.textViewTimestamp)

        /**
         * Binds the Location data to the views.
         */
        fun bind(location: Location) {
            nameTextView.text = location.name

            // Format the timestamp into a readable date string
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            timeTextView.text = dateFormat.format(Date(location.timestamp))

            // Set click listener to handle item clicks
            itemView.setOnClickListener {
                onItemClick(location)
            }
        }
    }

    /**
     * DiffUtil callback for calculating the difference between two non-null items in a list.
     */
    private class LocationDiffCallback : DiffUtil.ItemCallback<Location>() {
        // Check if two items represent the same location (based on unique ID)
        override fun areItemsTheSame(oldItem: Location, newItem: Location): Boolean {
            return oldItem.id == newItem.id
        }

        // Check if the content of two items is the same
        override fun areContentsTheSame(oldItem: Location, newItem: Location): Boolean {
            return oldItem == newItem
        }
    }
}
