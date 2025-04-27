package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.RideHistoryItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RideHistoryCard(
    ride: RideHistoryItem,
    onClick: () -> Unit = {}
) {
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
            
            Text(
                text = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(ride.startTime)),
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Distance",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = String.format("%.2f km", ride.distance),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    Text(
                        text = "Durée",
                        style = MaterialTheme.typography.bodySmall
                    )
                    val duration = ride.endTime?.let { it - ride.startTime } ?: 0L
                    Text(
                        text = "${duration / 60000} min",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column {
                    Text(
                        text = "Prix",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = String.format("%.2f €", ride.price),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
} 