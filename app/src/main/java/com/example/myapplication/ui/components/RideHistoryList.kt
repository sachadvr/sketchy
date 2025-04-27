package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.RideHistoryItem
import com.example.myapplication.data.model.RideStatus
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RideHistoryList(
    modifier: Modifier = Modifier,
    rides: List<RideHistoryItem>,
    onRideClick: (RideHistoryItem) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(16.dp)
    ) {
        items(rides.sortedByDescending { it.startTime }) { ride ->
            RideHistoryItemCard(
                ride = ride,
                onClick = { onRideClick(ride) }
            )
        }
    }
}

@Composable
private fun RideHistoryItemCard(
    ride: RideHistoryItem,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val startDate = Date(ride.startTime)
    val duration = ride.endTime?.let { endTime ->
        val durationMillis = endTime - ride.startTime
        String.format("%d min %d sec",
            durationMillis / 60000,
            (durationMillis % 60000) / 1000)
    } ?: "En cours"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = dateFormat.format(startDate),
                    style = MaterialTheme.typography.titleMedium
                )
                StatusChip(status = ride.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Durée: $duration",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = String.format("%.2f€", ride.price),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (ride.distance > 0) {
                Text(
                    text = String.format("Distance: %.2f km", ride.distance),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: RideStatus) {
    val (color, text) = when (status) {
        RideStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary to "En cours"
        RideStatus.COMPLETED -> MaterialTheme.colorScheme.secondary to "Terminée"
        RideStatus.CANCELLED -> MaterialTheme.colorScheme.error to "Annulée"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
} 