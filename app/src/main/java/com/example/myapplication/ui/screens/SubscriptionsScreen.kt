package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionsScreen(
    subscriptions: List<SubscriptionItem>,
    currentSubscription: SubscriptionItem?,
    onSubscribe: (SubscriptionItem) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SkatyPass") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            items(subscriptions) { subscription ->
                SubscriptionCard(
                    subscription = subscription,
                    isSubscribed = currentSubscription?.id == subscription.id,
                    onSubscribe = { onSubscribe(subscription) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionCard(
    subscription: SubscriptionItem,
    isSubscribed: Boolean,
    onSubscribe: () -> Unit
) {
    val typeColor = when (subscription.type) {
        "Premium" -> MaterialTheme.colorScheme.secondary
        "Standard" -> MaterialTheme.colorScheme.tertiary
        "Basique" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = subscription.name,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subscription.type,
                color = typeColor,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subscription.description,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "${subscription.price}€/${subscription.period}",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSubscribe,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSubscribed  
            ) {
                Text(if (isSubscribed) "Abonnement actif" else "S'abonner")
            }
        }
    }
}

data class SubscriptionItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val period: String,
    val features: List<String>,
    val type: String
)
