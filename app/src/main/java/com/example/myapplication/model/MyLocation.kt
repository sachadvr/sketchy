package com.example.myapplication.model

import kotlinx.serialization.Serializable
import org.osmdroid.util.GeoPoint

@Serializable
data class MyLocation(
    val latitude: Double,
    val longitude: Double
) {
    fun toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)
    
    companion object {
        fun fromGeoPoint(geoPoint: GeoPoint): MyLocation = 
            MyLocation(geoPoint.latitude, geoPoint.longitude)
    }
} 