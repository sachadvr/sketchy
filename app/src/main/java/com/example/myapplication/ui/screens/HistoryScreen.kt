package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.PricingCalculator
import com.example.myapplication.data.model.RideHistoryItem
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    rides: List<RideHistoryItem>,
    onRideClick: (RideHistoryItem) -> Unit
) {
    val locale = Locale.getDefault()
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy HH:mm", locale) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Historique des courses",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        if (rides.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aucune course effectuée",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rides) { ride ->
                    RideHistoryCard(
                        ride = ride,
                        dateFormat = dateFormat,
                        onClick = { onRideClick(ride) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RideHistoryCard(
    ride: RideHistoryItem,
    dateFormat: SimpleDateFormat,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
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
                    text = dateFormat.format(Date(ride.startTime)),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = PricingCalculator.formatPrice(ride.price),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val durationMs = ride.endTime?.let { it - ride.startTime } ?: 0L
                val minutes = (durationMs / 60000.0).toFloat()
                Text(
                    text = "Durée: ${String.format("%.1f", minutes)} min",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Distance: ${String.format("%.2f", ride.distance)} km",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
} 