package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.data.model.Skate
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color

@Composable
fun SkateList(
    modifier: Modifier = Modifier,
    skates: List<Skate>,
    onSkateClick: (Skate) -> Unit
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        items(skates) { skate ->
            SkateItem(
                skate = skate,
                onClick = { onSkateClick(skate) }
            )
        }
    }
}

@Composable
private fun SkateItem(
    skate: Skate,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = when (skate.status) {
                            "AVAILABLE" -> MaterialTheme.colorScheme.primary
                            "IN_USE" -> MaterialTheme.colorScheme.error
                            "MAINTENANCE" -> MaterialTheme.colorScheme.secondary
                            "OUT_OF_SERVICE" -> MaterialTheme.colorScheme.outline
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Skate #${skate.id}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Modèle: ${skate.model}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Status: ${skate.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = when (skate.status) {
                        "AVAILABLE" -> MaterialTheme.colorScheme.primary
                        "IN_USE" -> MaterialTheme.colorScheme.error
                        "MAINTENANCE" -> MaterialTheme.colorScheme.secondary
                        "OUT_OF_SERVICE" -> MaterialTheme.colorScheme.outline
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
} 