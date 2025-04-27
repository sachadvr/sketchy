package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Skate(
    val id: String,
    val serial_number: String,
    val model: String,
    val status: String,
    val coordinates: Coordinates? = null,
    val created_at: String? = null
)

@Serializable
data class Coordinates(
    val latitude: Double,
    val longitude: Double
)

@Serializable
data class CoordinatesDto(
    val latitude: Double,
    val longitude: Double
)

enum class SkateStatus {
    AVAILABLE,
    IN_USE,
    MAINTENANCE,
    OUT_OF_SERVICE
} 