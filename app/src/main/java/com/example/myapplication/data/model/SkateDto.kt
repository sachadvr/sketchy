package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SkateDto(
    val id: String,
    val serial_number: String,
    val model: String,
    val status: String,
    val created_at: String,
    val coordonnees: Map<String, Double>? = null,
    val battery_level: Int? = null
)

@Serializable
data class SkateCreateDto(
    val id: String,
    val serial_number: String,
    val model: String,
    val status: String,
    val coordonnees: CoordonneesDto
)

@Serializable
data class CoordonneesDto(
    val lat: Double,
    val lon: Double
)

fun SkateDto.toSkate(): Skate {
    val coords = coordonnees ?: mapOf("latitude" to 50.6292, "longitude" to 3.0573) 
    return Skate(
        id = id,
        serial_number = serial_number,
        model = model,
        status = status,
        coordinates = Coordinates(
            latitude = coords["latitude"] ?: 50.6292,
            longitude = coords["longitude"] ?: 3.0573
        ),
        created_at = created_at
    )
} 