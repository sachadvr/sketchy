package com.example.myapplication.data

import org.osmdroid.util.GeoPoint
import java.util.*

class RideHistory {
    private val rides = mutableListOf<RideHistoryItem>()

    fun addRide(ride: RideHistoryItem) {
        rides.add(0, ride)
    }

    fun getRides(): List<RideHistoryItem> = rides

    fun getRideById(id: String): RideHistoryItem? {
        return rides.find { it.id == id }
    }

    fun clearHistory() {
        rides.clear()
    }
}

data class RideHistoryItem(
    val id: String,
    val date: Date,
    val distance: Double,
    val duration: Long,
    val price: Double,
    val path: List<GeoPoint>
) 