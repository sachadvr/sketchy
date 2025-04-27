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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = skate.model,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Batterie: ${skate.batteryLevel}%",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (skate.disponible) {
                Text(
                    text = "Disponible",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium
                )
            } else {
                Text(
                    text = "Indisponible",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
} 