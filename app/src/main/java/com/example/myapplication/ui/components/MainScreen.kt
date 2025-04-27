package com.example.myapplication.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.myapplication.data.model.SubscriptionPlan
import com.example.myapplication.data.model.SubscriptionType
import com.example.myapplication.data.UserSubscription
import com.example.myapplication.SubscriptionPlanDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    supabaseClient: SupabaseClient,
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
    var currentSubscriptionPlans by remember { mutableStateOf<List<SubscriptionPlan>>(emptyList()) }
    var isAuthenticated by remember { mutableStateOf(supabaseClient.auth.currentUserOrNull() != null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        supabaseClient.auth.sessionStatus.collect { status ->
            isAuthenticated = when (status) {
                is SessionStatus.Authenticated -> true
                is SessionStatus.NotAuthenticated -> false
                else -> false
            }
        }
    }

    LaunchedEffect(Unit) {
        try {
            // On récupère d'abord les données Supabase dans une structure temporaire
            val supabasePlans = supabaseClient.postgrest["subscription_plans"]
                .select()
                .decodeList<SubscriptionPlanDto>()
            
            // Puis on les convertit vers notre modèle local
            val plans = supabasePlans.map { dto ->
                SubscriptionPlan(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description ?: "",
                    pricePerMonth = dto.price,
                    features = dto.features,
                    type = when (dto.type.uppercase()) {
                        "PREMIUM" -> SubscriptionType.PREMIUM
                        "UNLIMITED" -> SubscriptionType.UNLIMITED
                        else -> SubscriptionType.BASIC
                    }
                )
            }
            
            currentSubscriptionPlans = plans
            Log.d("DEBUG", "MainScreen → abonnements : ${plans.size}")
        } catch (e: Exception) {
            Log.e("DEBUG", "Erreur lors du chargement des abonnements", e)
        }
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Map) }
    var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
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
                        val subscriptionItems = currentSubscriptionPlans.map { plan ->
                            SubscriptionItem(
                                id = plan.id,
                                name = plan.name,
                                description = plan.description,
                                price = plan.pricePerMonth,
                                type = plan.type.toString(),
                                period = "MONTHLY", // Valeur par défaut
                                features = plan.features
                            )
                        }
                        SubscriptionsScreen(
                            subscriptions = subscriptionItems,
                            currentSubscription = currentSubscription,
                            onSubscribe = { subscription ->
                                val userId = supabaseClient.auth.currentUserOrNull()?.id
                                val today = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date.toString()

                                if (userId == null) {
                                    Log.e("SUB", "❌ Impossible de récupérer l'ID utilisateur")
                                    return@SubscriptionsScreen
                                }

                                val newSub = UserSubscription(
                                    user_id = userId,
                                    subscription_plan_id = subscription.id,
                                    start_date = today
                                )

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        supabaseClient.postgrest["user_subscriptions"]
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
                
                // Bouton de déconnexion
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.width(200.dp)
                ) {
                    Text("Se déconnecter")
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Déconnexion") },
            text = { Text("Voulez-vous vraiment vous déconnecter ?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        scope.launch {
                            supabaseClient.auth.signOut()
                            onLogout()
                        }
                    }
                ) {
                    Text("Oui")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Non")
                }
            }
        )
    }
}