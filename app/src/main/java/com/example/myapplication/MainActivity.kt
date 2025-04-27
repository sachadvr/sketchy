package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.appcompat.app.AlertDialog
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.PricingCalculator
import com.example.myapplication.data.model.RideHistoryItem
import com.example.myapplication.ui.components.MainScreen
import org.osmdroid.util.GeoPoint
import java.util.*
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.gotrue.auth
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.runtime.*
import com.example.myapplication.ui.components.AppContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import io.github.jan.supabase.SupabaseClient
import com.example.myapplication.data.model.SubscriptionPlan
import com.example.myapplication.data.repository.RideRepository
import androidx.compose.runtime.collectAsState
import com.example.myapplication.ui.viewmodel.MainViewModel
import androidx.activity.viewModels
import com.example.myapplication.data.model.GeoPointSerializable
import android.widget.Toast
import com.example.myapplication.ui.theme.AppTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.model.Skate
import com.example.myapplication.data.model.Coordinates
import com.example.myapplication.data.repository.SkateDto
import com.example.myapplication.data.repository.toSkate
import com.example.myapplication.data.model.SubscriptionPlanDto
import com.example.myapplication.data.model.toSubscriptionPlan
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.RegisterScreen
import io.github.jan.supabase.gotrue.SessionStatus

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient
    
    @Inject
    lateinit var rideRepository: RideRepository
    
    private val viewModel: MainViewModel by viewModels()

    private var isRideActive by mutableStateOf(false)
    private var startTime by mutableStateOf(0L)
    private var elapsedTime by mutableStateOf(0L)
    private var currentPath by mutableStateOf(listOf<GeoPoint>())
    private val rideHistory = mutableListOf<RideHistoryItem>()
    private val LOCATION_PERMISSION_REQUEST = 100
    
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRideActive) {
                elapsedTime = SystemClock.elapsedRealtime() - startTime
                mainHandler.postDelayed(this, 1000)
            }
        }
    }
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestLocationPermission()

        setContent {
            LaunchedEffect(Unit) {
                val user = supabaseClient.auth.currentUserOrNull()
                Log.d("AUTH_DEBUG", "User actuel: ${user?.email ?: "Non connecté"}")
                Log.d("AUTH_DEBUG", "User ID: ${user?.id ?: "Aucun ID"}")
                Log.d("AUTH_DEBUG", "Session valide: ${user != null}")
            }
            
            
            val uiState by viewModel.uiState.collectAsState()
            
            AppTheme {
                AppNavigation(
                    supabaseClient = supabaseClient,
                    isRideActive = uiState.currentRide != null,
                    elapsedTime = if (uiState.currentRide != null) {
                        val ride = uiState.currentRide!!
                        System.currentTimeMillis() - ride.startTime
                    } else {
                        0L
                    },
                    onStartRide = { location ->
                        Log.d("MainActivity", "StartRide appelé avec location $location")
                        viewModel.startRide("selected_skate", location)
                    },
                    onEndRide = {
                        viewModel.endRide()
                    },
                    onLogout = {
                        CoroutineScope(Dispatchers.IO).launch {
                            supabaseClient.auth.signOut()
                        }
                    },
                    onDeleteAccount = {
                        showDeleteAccountConfirmation()
                    },
                    rideHistory = uiState.rideHistory,
                    onRideClick = ::showRideDetails,
                    subscriptionPlans = uiState.subscriptionPlans
                )
            }
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showRideDetails(ride: RideHistoryItem) {
        val endTimeMs = ride.endTime ?: System.currentTimeMillis()
        val minutes = (endTimeMs - ride.startTime) / 60000f
        AlertDialog.Builder(this)
            .setTitle("Détails de la course")
            .setMessage("""
                Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(ride.startTime))}
                Distance: ${String.format("%.2f", ride.distance)} km
                Durée: ${String.format("%.1f", minutes)} minutes
                Prix: ${String.format("%.2f", ride.price)}€
            """.trimIndent())
            .setPositiveButton("OK", null)
            .show()
    }

    private fun calculateDistance(path: List<GeoPoint>): Double {
        if (path.size < 2) return 0.0
        
        var totalDistance = 0.0
        for (i in 1 until path.size) {
            totalDistance += path[i].distanceToAsDouble(path[i - 1])
        }
        return totalDistance / 1000.0 
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                println("Autorisé");
            }
        }
    }

    private fun afficherSkatesSurCarte(skates: List<Skate>, mapView: MapView) {
        skates.forEach { skate ->
            val coords = skate.coordinates ?: Coordinates(50.62925, 3.057256)
            val point = GeoPoint(coords.latitude, coords.longitude)
            val marker = Marker(mapView)
            marker.position = point
            marker.title = "Skate #${skate.id}"
            marker.snippet = "Modèle: ${skate.model}"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

            marker.icon = ResourcesCompat.getDrawable(resources, R.drawable.skate, null)

            mapView.overlays.add(marker)
        }
        mapView.controller.setCenter(GeoPoint(50.62925, 3.057256))
        mapView.controller.setZoom(15.0)
    }

    fun testFetchSkates(mapView: MapView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val skatesDto = supabaseClient.postgrest["skates"]
                    .select()
                    .decodeList<SkateDto>()
                
                
                val skates = skatesDto.map { dto -> 
                    dto.toSkate()
                }

                this@MainActivity.runOnUiThread {
                    afficherSkatesSurCarte(skates, mapView)
                }

            } catch (e: Exception) {
                Log.e("SUPABASE", "Erreur: ${e.message}", e)
            }
        }
    }

    fun fetchSubscriptionPlans(onResult: (List<SubscriptionPlan>) -> Unit) {
        Log.d("DEBUG", "🔥 fetchSubscriptionPlans lancé")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                
                val supabasePlans = supabaseClient
                    .postgrest["subscription_plans"]
                    .select()
                    .decodeList<SubscriptionPlanDto>()
                
                val plans = supabasePlans.map { dto ->
                    dto.toSubscriptionPlan()
                }

                Log.d("DEBUG", "Taille totale reçue : ${plans.size}")
                plans.forEach { plan ->
                    Log.d("DEBUG", "📦 ${plan.name} - ${plan.pricePerMonth}€ - ${plan.type}")
                }

                runOnUiThread {
                    onResult(plans)
                }

            } catch (e: Exception) {
                Log.e("SUPABASE", "Erreur fetch abonnements: ${e.message}", e)
            }
        }
    }

    private fun showDeleteAccountConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Supprimer votre compte")
            .setMessage("Êtes-vous sûr de vouloir supprimer votre compte? Cette action est irréversible.")
            .setPositiveButton("Supprimer") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Annuler", null)
            .show()
    }

    private fun deleteAccount() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userRepository = com.example.myapplication.data.repository.UserRepository(supabaseClient)
                val success = userRepository.deleteAccount()
                
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this@MainActivity, "Compte supprimé avec succès", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Erreur lors de la suppression du compte", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Erreur lors de la suppression du compte", e)
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Erreur: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

data class SubscriptionItem(
    val id: String,
    val duration: String,
    val description: String,
    val price: Double,
    val period: String,
    val tags: List<String>,
    val name: String
)

@Composable
fun AppNavigation(
    supabaseClient: SupabaseClient,
    isRideActive: Boolean,
    elapsedTime: Long,
    onStartRide: (GeoPoint) -> Unit,
    onEndRide: () -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    rideHistory: List<com.example.myapplication.data.model.RideHistoryItem>,
    onRideClick: (com.example.myapplication.data.model.RideHistoryItem) -> Unit,
    subscriptionPlans: List<SubscriptionPlan>
) {
    val navController = rememberNavController()
    
    val isAuthenticated = remember {
        mutableStateOf(supabaseClient.auth.currentUserOrNull() != null)
    }
    
    LaunchedEffect(Unit) {
        supabaseClient.auth.sessionStatus.collect { status ->
            isAuthenticated.value = status is SessionStatus.Authenticated
            Log.d("AUTH_DEBUG", "Changement d'état de session: $status")
        }
    }
    
    val startDestination = if (isAuthenticated.value) "main" else "login"
    
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                supabaseClient = supabaseClient,
                onLoginSuccess = {
                    Log.d("AUTH_DEBUG", "Login réussi, navigation vers main")
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate("register")
                }
            )
        }
        
        composable("register") {
            RegisterScreen(
                supabaseClient = supabaseClient,
                onRegisterSuccess = {
                    Log.d("AUTH_DEBUG", "Inscription réussie, navigation vers login")
                    navController.navigate("login")
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }

        composable("main") {
            MainScreen(
                supabaseClient = supabaseClient,
                isRideActive = isRideActive,
                onStartRide = onStartRide,
                onEndRide = onEndRide,
                elapsedTime = elapsedTime,
                onLogout = onLogout,
                onDeleteAccount = onDeleteAccount,
                rideHistory = rideHistory,
                onRideClick = onRideClick,
                subscriptionPlans = subscriptionPlans
            )
        }
    }
}

@Serializable
data class LocationPoint(
    val lat: Double,
    val lon: Double
)

@Serializable
data class RideData(
    val id: String,
    val user_id: String?,
    val start_time: Long,
    val end_time: Long?,
    val distance: Double,
    val duration: Long,
    val price: Double,
    val start_location: LocationPoint,
    val end_location: LocationPoint,
    val path: List<LocationPoint>,
    val created_at: Long
)

