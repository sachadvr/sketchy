package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Skate(
    val id: String,
    val model: String,
    val batteryLevel: Int,
    val disponible: Boolean = true,
    val coordonnees: Map<String, Double>? = null,
    val lastMaintenance: Long? = null,
    val status: SkateStatus = SkateStatus.AVAILABLE
)

enum class SkateStatus {
    AVAILABLE,
    IN_USE,
    MAINTENANCE,
    OUT_OF_SERVICE
} 