package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import com.example.myapplication.ui.screens.HistoryScreen
import com.example.myapplication.ui.screens.SubscriptionsScreen
import com.example.myapplication.ui.screens.SettingsScreen
import com.example.myapplication.data.RideHistoryItem
import com.example.myapplication.ui.screens.SubscriptionItem

sealed class Screen {
    object Map : Screen()
    object History : Screen()
    object Subscriptions : Screen()
    object Settings : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    isRideActive: Boolean,
    onStartRide: (GeoPoint) -> Unit,
    onEndRide: () -> Unit,
    elapsedTime: Long,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    rideHistory: List<RideHistoryItem>,
    onRideClick: (RideHistoryItem) -> Unit
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Map) }
    var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sidebarItems = listOf(
        SidebarItem("Carte", Icons.Default.MoreVert) {
            currentScreen = Screen.Map
            scope.launch { drawerState.close() }
        },
        SidebarItem("Historique", Icons.Default.Menu) {
            currentScreen = Screen.History
            scope.launch { drawerState.close() }
        },
        SidebarItem("Abonnements", Icons.Default.AccountBox) {
            currentScreen = Screen.Subscriptions
            scope.launch { drawerState.close() }
        },
        SidebarItem("Réglages", Icons.Default.Settings) {
            currentScreen = Screen.Settings
            scope.launch { drawerState.close() }
        }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Sidebar(
                    items = sidebarItems,
                    modifier = Modifier.width(300.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            when (currentScreen) {
                                Screen.Map -> "SkateApp"
                                Screen.History -> "Historique"
                                Screen.Subscriptions -> "Abonnements"
                                Screen.Settings -> "Réglages"
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                drawerState.open()
                            }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentScreen) {
                    Screen.Map -> MapScreen(
                        isRideActive = isRideActive,
                        onStartRide = onStartRide,
                        onEndRide = onEndRide,
                        elapsedTime = elapsedTime
                    )
                    Screen.History -> HistoryScreen(
                        rides = rideHistory,
                        onRideClick = onRideClick
                    )
                    Screen.Subscriptions -> SubscriptionsScreen(
                        subscriptions = listOf(
                            SubscriptionItem("1", "60 min", "À utiliser sous 3 jours", 7.99, "mois", listOf("Skate", "Abonnement"), "Basique"),
                            SubscriptionItem("2", "200 min", "à utiliser sous 7 jours", 23.99, "mois", listOf("Skate", "Abonnement"), "Standard"),
                            SubscriptionItem("3", "300 min", "à utiliser sous 30 jours", 34.99, "mois", listOf("Skate", "Abonnement"), "Premium")
                        ),
                        onSubscribe = { subscription -> println("Abonné à ${subscription.name}") }
                    )
                    Screen.Settings -> SettingsScreen(
                        onLogout = onLogout,
                        onDeleteAccount = onDeleteAccount
                    )
                }
            }
        }
    }
}