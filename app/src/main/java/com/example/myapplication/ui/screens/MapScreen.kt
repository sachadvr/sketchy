package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import org.osmdroid.util.GeoPoint

@Composable
fun MapScreen(
    _isRideActive: Boolean,
    _onStartRide: (GeoPoint) -> Unit,
    _onEndRide: () -> Unit,
    _elapsedTime: Long
) {
    Column(modifier = Modifier.fillMaxSize()) {
        IconButton(onClick = { /* Action pour centrer sur la position */ }) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Ma position"
            )
        }
        // ... reste du code ...
    }
} 