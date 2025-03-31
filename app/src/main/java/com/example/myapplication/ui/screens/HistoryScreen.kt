package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*
import org.osmdroid.util.GeoPoint
import com.example.myapplication.data.RideHistoryItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    rides: List<RideHistoryItem>,
    onRideClick: (RideHistoryItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historique des courses") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(rides) { ride ->
                RideHistoryCard(
                    ride = ride,
                    onClick = { onRideClick(ride) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryCard(
    ride: RideHistoryItem,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = formatDate(ride.date),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Distance: ${ride.distance}km",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Durée: ${formatDuration(ride.duration)}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

private fun formatDate(date: Date): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(date)
}

private fun formatDuration(duration: Long): String {
    val hours = duration / 3600
    val minutes = (duration % 3600) / 60
    val seconds = duration % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
} 