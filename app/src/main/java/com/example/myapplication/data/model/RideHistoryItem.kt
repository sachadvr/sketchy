package com.example.myapplication.data.model

import kotlinx.serialization.Serializable
import org.osmdroid.util.GeoPoint
import java.util.Date

@Serializable
data class RideHistoryItem(
    val id: String,
    val userId: String,
    val skateId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val distance: Double = 0.0,
    val price: Double = 0.0,
    val path: List<GeoPointSerializable> = emptyList(),
    val status: RideStatus = RideStatus.IN_PROGRESS
)

enum class RideStatus {
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
} 