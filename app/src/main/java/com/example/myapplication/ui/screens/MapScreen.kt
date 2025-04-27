package com.example.myapplication.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import org.osmdroid.util.GeoPoint
import com.example.myapplication.ui.components.MapScreen as ComponentMapScreen
import com.example.myapplication.data.model.Skate

@Composable
fun MapScreen(
    isRideActive: Boolean = false,
    onStartRide: (GeoPoint) -> Unit = {},
    onEndRide: () -> Unit = {},
    elapsedTime: Long = 0,
    availableSkates: List<Skate> = emptyList()
) {
    val minutes = (elapsedTime / 60000).toInt()
    val seconds = ((elapsedTime % 60000) / 1000).toInt()
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)
    
    Log.d("MapScreen (wrapper)", "Affichage: isRideActive=$isRideActive, elapsedTime=$elapsedTime, timeFormatted=$timeFormatted")
    
    
    ComponentMapScreen(
        isRideActive = isRideActive,
        onStartRide = { 
            Log.d("MapScreen (wrapper)", "onStartRide appelé avec position $it")
            onStartRide(it) 
        },
        onEndRide = { 
            Log.d("MapScreen (wrapper)", "onEndRide appelé")
            onEndRide() 
        },
        elapsedTime = timeFormatted,
        availableSkates = availableSkates
    )
} 