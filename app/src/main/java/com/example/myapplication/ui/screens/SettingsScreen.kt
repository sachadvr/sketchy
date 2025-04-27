package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Réglages") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section Profil
            Text(
                text = "Profil",
                style = MaterialTheme.typography.titleLarge
            )
            
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .padding(horizontal = 16.dp)
            ) {
                Text("Se déconnecter 1")
            }

            // Section Compte
            Text(
                text = "Compte",
                style = MaterialTheme.typography.titleLarge
            )
            
            OutlinedButton(
                onClick = onDeleteAccount,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Supprimer le compte")
            }

            // Section Notifications
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.titleLarge
            )
            
            var notificationsEnabled by remember { mutableStateOf(true) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Activer les notifications")
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }

            Text(
                text = "Apparence",
                style = MaterialTheme.typography.titleLarge
            )
            
            var darkModeEnabled by remember { mutableStateOf(false) }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Mode sombre")
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = { darkModeEnabled = it }
                )
            }
        }
    }
} 