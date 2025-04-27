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
import com.example.myapplication.data.model.RideHistoryItem
import com.example.myapplication.data.model.SubscriptionPlan
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.RegisterScreen

@Composable
fun AppContent(
    supabaseClient: SupabaseClient,
    isLoggedIn: Boolean,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    isRideActive: Boolean = false,
    elapsedTime: Long = 0,
    onStartRide: (GeoPoint) -> Unit = {},
    onEndRide: () -> Unit = {},
    rideHistory: List<com.example.myapplication.data.model.RideHistoryItem> = emptyList(),
    onRideClick: (com.example.myapplication.data.model.RideHistoryItem) -> Unit = {},
    subscriptionPlans: List<SubscriptionPlan> = emptyList()
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
        
        user == null && !showRegister -> Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            LoginScreen(
                supabaseClient = supabaseClient,
                onLoginSuccess = {
                }
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { showRegister = true }) {
                Text("Pas de compte ? Inscris-toi")
            }
        }

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
                },
                onLoginClick = { showRegister = false }
            )
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = { showRegister = false }) {
                Text("Déjà inscrit ? Connecte-toi")
            }
        }

        else -> MainScreen(
            supabaseClient = supabaseClient,
            isRideActive = elapsedTime > 0, // considérer qu'une course est active si le temps est supérieur à 0
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
