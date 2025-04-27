package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus
import org.osmdroid.util.GeoPoint
import com.example.myapplication.data.RideHistoryItem
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.RegisterScreen

@Composable
fun AppContent(
    supabaseClient: SupabaseClient,
    rideHistory: List<RideHistoryItem>,
    onRideClick: (RideHistoryItem) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    onStartRide: (GeoPoint) -> Unit,
    onEndRide: () -> Unit,
    elapsedTime: Long
) {
    var showRegister by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(supabaseClient.auth.currentUserOrNull() != null) }

    LaunchedEffect(Unit) {
        supabaseClient.auth.sessionStatus.collect { status ->
            isAuthenticated = when (status) {
                is SessionStatus.Authenticated -> true
                is SessionStatus.NotAuthenticated -> false
                else -> false
            }
        }
    }

    val user = supabaseClient.auth.currentUserOrNull()

    when {
        // 1️⃣ pas de user et on est en mode login
        user == null && !showRegister -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            LoginScreen(
                supabaseClient = supabaseClient,
                onLoginSuccess = {
                    /* après login, Compose recomposera automatiquement AppContent */
                }
            )
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
            RegisterScreen(
                supabaseClient = supabaseClient,
                onRegisterSuccess = {
                    showRegister = false
                }
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { showRegister = false }) {
                Text("Déjà inscrit ? Connecte-toi")
            }
        }

        // 3️⃣ user connecté -> on affiche MainScreen
        else -> MainScreen(
            supabaseClient = supabaseClient,
            isRideActive = false,
            onStartRide = onStartRide,
            onEndRide = onEndRide,
            elapsedTime = elapsedTime,
            onLogout = onLogout,
            onDeleteAccount = onDeleteAccount,
            rideHistory = rideHistory,
            onRideClick = onRideClick,
            subscriptionPlans = emptyList()
        )
    }
}
