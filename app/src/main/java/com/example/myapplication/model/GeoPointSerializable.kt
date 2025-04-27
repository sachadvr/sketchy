package com.example.myapplication.model

import kotlinx.serialization.Serializable
import org.osmdroid.util.GeoPoint

@Serializable
data class GeoPointSerializable(
    val latitude: Double,
    val longitude: Double
) {
    fun toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

    companion object {
        fun fromGeoPoint(geoPoint: GeoPoint): GeoPointSerializable =
            GeoPointSerializable(geoPoint.latitude, geoPoint.longitude)
    }
} 