package com.example.myapplication.ui.components

import android.text.format.DateUtils.formatElapsedTime
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import com.example.myapplication.ui.screens.HistoryScreen
import com.example.myapplication.ui.screens.SubscriptionsScreen
import com.example.myapplication.ui.screens.SettingsScreen
import com.example.myapplication.data.SubscriptionManager
import com.example.myapplication.ui.screens.SubscriptionItem
import com.example.myapplication.data.model.SubscriptionPlan
import com.example.myapplication.data.model.SubscriptionType
import com.example.myapplication.data.UserSubscription
import com.example.myapplication.data.model.SubscriptionPlanDto
import com.example.myapplication.data.model.toSubscriptionPlan
import com.example.myapplication.data.model.Skate
import com.example.myapplication.data.model.SkateStatus
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.gotrue.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import kotlin.random.Random
import org.osmdroid.views.MapView
import com.example.myapplication.data.model.SkateDto
import com.example.myapplication.data.model.toSkate
import com.example.myapplication.ui.screens.MapScreen
import com.example.myapplication.ui.theme.AppTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import com.example.myapplication.data.model.RideHistoryItem
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import java.util.UUID
import androidx.compose.material3.CircularProgressIndicator
import android.widget.Toast
import io.github.jan.supabase.postgrest.query.Order
import com.example.myapplication.data.model.UserSubscriptionDto
import com.example.myapplication.data.model.SubscriptionPlanDetailsDto

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
    rideHistory: List<com.example.myapplication.data.model.RideHistoryItem>,
    onRideClick: (com.example.myapplication.data.model.RideHistoryItem) -> Unit,
    subscriptionPlans: List<SubscriptionPlan>,
    availableSkates: List<Skate> = emptyList()
) {
    var currentSubscriptionPlans by remember { mutableStateOf<List<SubscriptionPlan>>(emptyList()) }
    var isAuthenticated by remember { mutableStateOf(supabaseClient.auth.currentUserOrNull() != null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var isFirstLocation by remember { mutableStateOf(true) }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val markers = remember { mutableStateListOf<Marker>() }
    val lilleLocation = remember { GeoPoint(50.6292, 3.0573) }
    var currentPath by remember { mutableStateOf(listOf<GeoPoint>()) }

    var localRideActive by remember { mutableStateOf(isRideActive) }
    var rideActiveState by remember { mutableStateOf(isRideActive) }
    var rideStartTime by remember { mutableStateOf(0L) }
    var currentElapsedTime by remember { mutableStateOf(elapsedTime) }
    var formattedTime by remember { mutableStateOf("00:00") }

    LaunchedEffect(isRideActive) {
        localRideActive = isRideActive
        Log.d("MainScreen", "État de la course global modifié: isRideActive=$isRideActive → localRideActive=$localRideActive")
        
        if (isRideActive) {
            rideStartTime = System.currentTimeMillis() - elapsedTime
            Log.d("MainScreen", "Initialisation du timer: rideStartTime=$rideStartTime, elapsedTime=$elapsedTime")
        }
    }

    LaunchedEffect(localRideActive) {
        if (localRideActive) {
            if (rideStartTime == 0L) {
                rideStartTime = System.currentTimeMillis()
                Log.d("MainScreen", "Démarrage du timer: rideStartTime=$rideStartTime")
            }
            
            while (localRideActive) {
                currentElapsedTime = System.currentTimeMillis() - rideStartTime
                Log.d("MainScreen", "Mise à jour du timer: currentElapsedTime=$currentElapsedTime")
                delay(1000)
            }
        }
    }

    LaunchedEffect(currentElapsedTime) {
        val minutes = (currentElapsedTime / 60000).toInt()
        val seconds = ((currentElapsedTime % 60000) / 1000).toInt()
        formattedTime = String.format("%02d:%02d", minutes, seconds)
        Log.d("MainScreen", "Format du temps: $formattedTime (minutes=$minutes, seconds=$seconds)")
    }

    var skates by remember { mutableStateOf<List<Skate>>(emptyList()) }

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
            val supabasePlans = supabaseClient.postgrest["subscription_plans"]
                .select()
                .decodeList<SubscriptionPlanDto>()
            
            currentSubscriptionPlans = supabasePlans.map { it.toSubscriptionPlan() }
            Log.d("DEBUG", "MainScreen → abonnements chargés : ${currentSubscriptionPlans.size}")
        } catch (e: Exception) {
            Log.e("DEBUG", "Erreur lors du chargement des abonnements", e)
        }
    }

    LaunchedEffect(Unit) {
        try {
            val skatesDto = supabaseClient.postgrest["skates"]
                .select()
                .decodeList<SkateDto>()
            
            skates = skatesDto.map { it.toSkate() }
            Log.d("DEBUG", "MainScreen → skates chargés : ${skates.size}")
        } catch (e: Exception) {
            Log.e("DEBUG", "Erreur lors du chargement des skates", e)
        }
    }

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Map) }
    var drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    var showUserInfo by remember { mutableStateOf(false) }

    val sidebarItems = listOf(
        SidebarItem("Carte", Icons.Default.LocationOn) {
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
        gesturesEnabled = false,
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
                                Screen.Subscriptions -> "SkatyPass"
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
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
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
                    Screen.Map -> {
                        Log.d("MainScreen", "Affichage MapScreen: isRideActive=$isRideActive, localRideActive=$localRideActive, elapsedTime=$elapsedTime, formattedTime=$formattedTime")
                        MapScreen(
                            isRideActive = localRideActive,
                            onStartRide = { location -> 
                                Log.d("MainScreen", "StartRide appelé avec location $location")
                                onStartRide(location)
                                
                                rideActiveState = true
                                localRideActive = true
                                
                                rideStartTime = System.currentTimeMillis()
                                currentElapsedTime = 0L
                            },
                            onEndRide = { 
                                Log.d("MainScreen", "EndRide appelé")
                                onEndRide() 
                                
                                rideActiveState = false
                                localRideActive = false
                            },
                            
                            elapsedTime = formattedTime,
                            availableSkates = skates
                        )
                    }
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
                                period = "MONTHLY",
                                features = plan.features
                            )
                        }
                        SubscriptionsScreen(
                            subscriptions = subscriptionItems,
                            currentSubscription = null,
                            onSubscribe = { subscription ->
                                val userId = supabaseClient.auth.currentUserOrNull()?.id
                                val today = Clock.System.now()
                                    .toLocalDateTime(TimeZone.currentSystemDefault())
                                    .date.toString()

                                if (userId == null) {
                                    Log.e("SUB", "❌ Impossible de récupérer l'ID utilisateur")
                                    return@SubscriptionsScreen
                                }

                                
                                val newSub = mapOf(
                                    "id" to UUID.randomUUID().toString(),
                                    "user_id" to userId,
                                    "plan_id" to subscription.id,
                                    "start_date" to today,
                                    "status" to "active" 
                                )

                                scope.launch {
                                    try {
                                        supabaseClient.postgrest["user_subscriptions"]
                                            .insert(newSub)
                                        Log.d("SUB", "✅ Abonnement enregistré ! ${newSub["id"]}")
                                    } catch (e: Exception) {
                                        Log.e("SUB", "❌ Erreur d'enregistrement : ${e.message}", e)
                                    }
                                }
                            }
                        )
                    }
                    Screen.Profile -> {
                        ProfileScreen(
                            supabaseClient = supabaseClient,
                            onLogout = onLogout,
                            onDeleteAccount = onDeleteAccount
                        )
                    }
                    Screen.Settings -> SettingsScreen(
                        onLogout = { showLogoutDialog = true },
                        onDeleteAccount = onDeleteAccount
                    )
                }

                if (showUserInfo && currentScreen != Screen.Profile) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .width(280.dp)
                            .zIndex(10f),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Informations utilisateur",
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            )
                            
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            
                            val user = supabaseClient.auth.currentUserOrNull()
                            if (user != null) {
                                Text(
                                    text = "Email: ${user.email}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                    }
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

@Composable
private fun ProfileScreen(
    supabaseClient: SupabaseClient,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    
    var activeSubscription by remember { mutableStateOf<UserSubscriptionDto?>(null) }
    var subscriptionPlanDetails by remember { mutableStateOf<SubscriptionPlanDetailsDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    
    
    LaunchedEffect(Unit) {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
        if (userId != null) {
            try {
                val subscriptions = supabaseClient.postgrest["user_subscriptions"]
                    .select {
                        filter {
                            eq("user_id", userId)
                            eq("status", "active")
                        }
                        order("start_date", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                        limit(1)
                    }
                    .decodeList<UserSubscriptionDto>()
                
                if (subscriptions.isNotEmpty()) {
                    
                    activeSubscription = subscriptions.first()
                    val planId = activeSubscription?.plan_id
                    
                    if (planId != null) {
                        val planDetails = supabaseClient.postgrest["subscription_plans"]
                            .select {
                                filter { eq("id", planId) }
                            }
                            .decodeList<SubscriptionPlanDetailsDto>()
                            .firstOrNull()
                        
                        subscriptionPlanDetails = planDetails
                    }
                }
            } catch (e: Exception) {
                Log.e("ProfileScreen", "Erreur lors de la récupération de l'abonnement: ${e.message}", e)
                Toast.makeText(context, "Impossible de charger votre abonnement", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = supabaseClient.auth.currentUserOrNull()?.email?.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = supabaseClient.auth.currentUserOrNull()?.email ?: "Email non disponible",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Text(
                    text = "ID: ${supabaseClient.auth.currentUserOrNull()?.id?.take(8)}...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        
        
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Abonnement",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(16.dp)
                    )
                } else if (activeSubscription != null) {
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Statut:")
                        Text(
                            text = "Actif",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Plan:")
                        Text(
                            text = subscriptionPlanDetails?.name ?: "Plan Standard",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Prix:")
                        Text(
                            text = "${subscriptionPlanDetails?.price ?: "?"}€/mois",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            val subscriptionId = activeSubscription?.id
                            if (subscriptionId != null) {
                                scope.launch {
                                    try {
                                        
                                        supabaseClient.postgrest["user_subscriptions"]
                                            .update({
                                                set("status", "cancelled")
                                            }) {
                                                filter { eq("id", subscriptionId) }
                                            }
                                        
                                        
                                        activeSubscription = null
                                        subscriptionPlanDetails = null
                                        Toast.makeText(context, "Abonnement annulé avec succès", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Log.e("ProfileScreen", "Erreur lors de l'annulation de l'abonnement: ${e.message}", e)
                                        Toast.makeText(context, "Impossible d'annuler l'abonnement", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Annuler l'abonnement")
                    }
                } else {
                    Text(
                        text = "Aucun abonnement actif",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(16.dp)
                    )
                    
                    Button(
                        onClick = {
                            
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Voir les abonnements")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Se déconnecter")
        }
        
        Button(
            onClick = { showDeleteConfirmDialog = true },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text("Supprimer mon compte")
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Confirmation") },
            text = { Text("Voulez-vous vraiment supprimer votre compte? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteAccount()
                    }
                ) {
                    Text("Oui, supprimer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}
