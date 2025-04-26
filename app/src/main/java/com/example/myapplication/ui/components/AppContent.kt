package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.SupabaseManager
import io.github.jan.supabase.gotrue.gotrue
import org.osmdroid.util.GeoPoint
import com.example.myapplication.data.RideHistoryItem
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.RegisterScreen

@Composable
fun AppContent(
    rideHistory: List<RideHistoryItem>,
    onRideClick: (RideHistoryItem) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onStartRide: (GeoPoint) -> Unit,
    onEndRide: () -> Unit,
    elapsedTime: Long
) {
    var showRegister by remember { mutableStateOf(false) }
    val user = SupabaseManager.client.gotrue.currentUserOrNull()

    when {
        // 1️⃣ pas de user et on est en mode login
        user == null && !showRegister -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            LoginScreen(onLoginSuccess = {
                /* après login, Compose recomposera automatiquement AppContent */
            })
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { showRegister = true }) {
                Text("Pas de compte ? Inscris-toi")
            }
        }

        // 2️⃣ pas de user et on est en mode register
        user == null && showRegister -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            RegisterScreen(onRegisterSuccess = {
                showRegister = false
            })
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { showRegister = false }) {
                Text("Déjà inscrit ? Connecte-toi")
            }
        }

        // 3️⃣ user connecté -> on affiche MainScreen
        else -> MainScreen(
            isRideActive      = false,
            onStartRide       = onStartRide,
            onEndRide         = onEndRide,
            elapsedTime       = elapsedTime,
            onLogout          = onLogout,
            onDeleteAccount   = onDeleteAccount,
            rideHistory       = rideHistory,
            onRideClick       = onRideClick,
            subscriptionPlans = emptyList()
        )
    }
}
