package com.example.myapplication.ui.components

import android.util.Log
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
import com.example.myapplication.data.SubscriptionManager
import com.example.myapplication.ui.screens.SubscriptionItem
import com.example.myapplication.SubscriptionPlan
import com.example.myapplication.SupabaseManager
import com.example.myapplication.data.UserSubscription
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.coroutines.launch
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.gotrue.GoTrue


sealed class Screen {
    object Map : Screen()
    object History : Screen()
    object Subscriptions : Screen()
    object Settings : Screen()
    object Profile : Screen()
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
    onRideClick: (RideHistoryItem) -> Unit,
    subscriptionPlans: List<SubscriptionPlan>
) {

    var subscriptionPlans by remember { mutableStateOf<List<SubscriptionPlan>>(emptyList()) }

    LaunchedEffect(Unit) {
        SupabaseManager.client
            .postgrest["subscription_plans"]
            .select()
            .decodeList<SubscriptionPlan>()
            .let { plans ->
                subscriptionPlans = plans
                Log.d("DEBUG", "MainScreen → abonnements : ${plans.size}")
            }
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Map) }
    var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val subscriptionManager = remember { SubscriptionManager() }
    var currentSubscription by remember { mutableStateOf<SubscriptionItem?>(null) }

    val sidebarItems = listOf(
        SidebarItem("Carte", Icons.Default.MoreVert) {
            currentScreen = Screen.Map
            scope.launch { drawerState.close() }
        },
        SidebarItem("Profil", Icons.Default.Person) {
            currentScreen = Screen.Profile
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
                                Screen.Map -> "Sketchy"
                                Screen.History -> "Historique"
                                Screen.Subscriptions -> "Abonnements"
                                Screen.Profile -> "Mon Profil"
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
                    Screen.Subscriptions -> {
                        val subscriptionItems = subscriptionPlans.map {
                            SubscriptionItem(
                                id = it.id,
                                name = it.name,
                                description = it.description ?: "",
                                price = it.price,
                                type =it.type,
                                period = it.period,
                                features = it.features
                            )
                        }
                        SubscriptionsScreen(
                            subscriptions = subscriptionItems,
                            currentSubscription = currentSubscription,
                            onSubscribe = fun(subscription: SubscriptionItem) {
                                val userId = SupabaseManager.client.gotrue.currentUserOrNull()?.id
                                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()

                                if (userId == null) {
                                    Log.e("SUB", "❌ Impossible de récupérer l'ID utilisateur")
                                    return
                                }

                                val newSub = UserSubscription(
                                    user_id = userId,
                                    subscription_plan_id = subscription.id,
                                    start_date = today
                                )

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        SupabaseManager.client.postgrest["user_subscriptions"]
                                            .insert(newSub)
                                        Log.d("SUB", "✅ Abonnement enregistré !")
                                    } catch (e: Exception) {
                                        Log.e("SUB", "❌ Erreur d'enregistrement : ${e.message}", e)
                                    }
                                }
                            }

                        )
                    }
                    Screen.Profile -> ProfileScreen(
                        currentSubscription = currentSubscription,
                        onUnsubscribe = { subscription ->
                            subscriptionManager.unsubscribe(subscription)
                            currentSubscription = null
                            // Vous pouvez rediriger vers un autre écran, par exemple revenir aux abonnements
                            currentScreen = Screen.Subscriptions
                        },
                        onBack = { currentScreen = Screen.Subscriptions }
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