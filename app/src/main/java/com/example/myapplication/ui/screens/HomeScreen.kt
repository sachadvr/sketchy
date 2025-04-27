package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.SupabaseClient

/**
 * Écran d'accueil de l'application
 * @param supabaseClient instance du client Supabase
 * @param onLogout callback pour la déconnexion
 */
@Composable
fun HomeScreen(
    supabaseClient: SupabaseClient,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Bienvenue dans Sketchy!")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(onClick = onLogout) {
            Text("Se déconnecter")
        }
    }
} 