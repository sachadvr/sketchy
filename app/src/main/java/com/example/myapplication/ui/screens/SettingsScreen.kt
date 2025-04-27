package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.viewmodels.ThemeViewModel
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    themeViewModel: ThemeViewModel = hiltViewModel()
) {
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    
    
    LaunchedEffect(isDarkMode) {
        Log.d("SettingsScreen", "Mode sombre actuel: $isDarkMode")
    }

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
                Text("Se déconnecter")
            }

            
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

            Text(
                text = "Apparence",
                style = MaterialTheme.typography.titleLarge
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mode sombre",
                    style = MaterialTheme.typography.bodyLarge
                )
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { 
                        Log.d("SettingsScreen", "Switch toggled, nouvelle valeur: ${!isDarkMode}")
                        themeViewModel.toggleDarkMode() 
                    }
                )
            }
            
            
            Text(
                text = "Statut actuel: ${if (isDarkMode) "Mode sombre activé" else "Mode clair activé"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
} 