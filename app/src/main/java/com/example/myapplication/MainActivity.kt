package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.PricingCalculator
import com.example.myapplication.data.RideHistory
import com.example.myapplication.data.RideHistoryItem
import com.example.myapplication.ui.components.MainScreen
import org.osmdroid.util.GeoPoint
import java.util.*
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
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
import com.example.myapplication.data.model.SubscriptionType
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.screens.LoginScreen
import com.example.myapplication.ui.screens.RegisterScreen
import com.example.myapplication.ui.screens.HomeScreen

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var supabaseClient: SupabaseClient

    private var isRideActive by mutableStateOf(false)
    private var startTime by mutableStateOf(0L)
    private var elapsedTime by mutableStateOf(0L)
    private var currentPath by mutableStateOf(listOf<GeoPoint>())
    private val rideHistory = RideHistory()
    private val LOCATION_PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Demande des permissions de localisation
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST)
        }

        // Configuration de l'interface utilisateur avec Compose
        findViewById<androidx.compose.ui.platform.ComposeView>(R.id.composeView).setContent {
            // Variables pour stocker les données
            var subscriptionPlans by remember { mutableStateOf(listOf<SubscriptionPlan>()) }
            val rides: List<RideHistoryItem> = rideHistory.getRides()

            // Chargement des plans d'abonnement
            LaunchedEffect(Unit) {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val plans = supabaseClient.postgrest["subscription_plans"]
                            .select()
                            .decodeList<SubscriptionPlan>()
                        subscriptionPlans = plans
                    } catch (e: Exception) {
                        Log.e("SUPABASE", "Erreur lors du chargement des abonnements: ${e.message}", e)
                    }
                }
            }

            // Affichage du contenu principal de l'application
            AppContent(
                supabaseClient = supabaseClient,
                rideHistory = rides,
                onRideClick = { ride -> showRideDetails(ride) },
                onLogout = { 
                    // Déconnexion de l'utilisateur
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            supabaseClient.auth.signOut()
                        } catch (e: Exception) {
                            Log.e("SUPABASE", "Erreur lors de la déconnexion: ${e.message}", e)
                        }
                    }
                },
                onDeleteAccount = {
                    // Supprimer le compte utilisateur
                    AlertDialog.Builder(this)
                        .setTitle("Supprimer le compte")
                        .setMessage("Êtes-vous sûr de vouloir supprimer votre compte ? Cette action est irréversible.")
                        .setPositiveButton("Oui") { _, _ ->
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    // Supabase ne fournit pas de méthode deleteUser, utilisons signOut
                                    supabaseClient.auth.signOut()
                                    runOnUiThread {
                                        // Informer l'utilisateur
                                        AlertDialog.Builder(this@MainActivity)
                                            .setTitle("Compte déconnecté")
                                            .setMessage("Vous avez été déconnecté. Pour supprimer complètement votre compte, veuillez contacter le support.")
                                            .setPositiveButton("OK", null)
                                            .show()
                                    }
                                } catch (e: Exception) {
                                    Log.e("SUPABASE", "Erreur lors de la suppression du compte: ${e.message}", e)
                                }
                            }
                        }
                        .setNegativeButton("Non", null)
                        .show()
                },
                onStartRide = { location -> showStartRideConfirmation(location) },
                onEndRide = { showEndRideConfirmation() },
                elapsedTime = elapsedTime
            )
        }

        // Démarrage du thread de suivi du temps
        startTimeTrackingThread()
    }

    private fun startTimeTrackingThread() {
        Thread {
            while (true) {
                if (isRideActive) {
                    elapsedTime = SystemClock.elapsedRealtime() - startTime
                    // Ici, vous pourriez ajouter la position actuelle au chemin
                    // currentPath = currentPath + currentLocation
                }
                Thread.sleep(1000)
            }
        }.start()
    }

    private fun showStartRideConfirmation(location: GeoPoint) {
        AlertDialog.Builder(this)
            .setTitle("Confirmation de location")
            .setMessage("Voulez-vous commencer la location du skateboard à cet endroit ?")
            .setPositiveButton("Oui") { _, _ ->
                startRide(location)
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun startRide(location: GeoPoint) {
        isRideActive = true
        startTime = SystemClock.elapsedRealtime()
        currentPath = listOf(location)
    }

    private fun showEndRideConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Fin de la course")
            .setMessage("Voulez-vous vraiment terminer la course ?")
            .setPositiveButton("Oui") { _, _ ->
                endRide()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun endRide() {
        isRideActive = false
        val minutes = elapsedTime / 60000f
        val price = PricingCalculator.calculatePrice(minutes.toInt())
        
        // Créer un nouvel élément d'historique
        val ride = RideHistoryItem(
            id = UUID.randomUUID().toString(),
            date = Date(),
            distance = calculateDistance(currentPath),
            duration = elapsedTime,
            price = price,
            path = currentPath
        )
        
        // Ajouter à l'historique
        rideHistory.addRide(ride)
        
        AlertDialog.Builder(this)
            .setTitle("Course terminée (${PricingCalculator.formatPrice(price)})")
            .setMessage("Durée de la course : ${String.format("%.1f", minutes)} minutes")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showRideDetails(ride: RideHistoryItem) {
        val minutes = ride.duration / 60000f
        AlertDialog.Builder(this)
            .setTitle("Détails de la course")
            .setMessage("""
                Date: ${String.format("%tF %<tT", ride.date)}
                Distance: ${String.format("%.2f", ride.distance)} km
                Durée: ${String.format("%.1f", minutes)} minutes
                Prix: ${ride.price}€
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
        return totalDistance / 1000.0 // Conversion en kilomètres
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée
            }
        }
    }

    private fun afficherSkatesSurCarte(skates: List<Skate>, mapView: MapView) {
        skates.forEach { skate ->
            val coords = skate.coordonnees
            if (coords != null) {
                val lat = coords["lat"]
                val lon = coords["lon"]

                if (lat != null && lon != null) {
                    val point = GeoPoint(lat, lon)
                    val marker = Marker(mapView)
                    marker.position = point
                    marker.title = skate.model
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                    // Utiliser l'icône du skate
                    marker.icon = ResourcesCompat.getDrawable(resources, R.drawable.skate, null)

                    mapView.overlays.add(marker)
                }
            }
        }
        // Centrer la carte sur Lille
        mapView.controller.setCenter(GeoPoint(50.62925, 3.057256))
        mapView.controller.setZoom(15.0)
    }

    fun testFetchSkates(mapView: MapView) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val skates = supabaseClient.postgrest["skates"]
                    .select()
                    .decodeList<Skate>()

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
                // On récupère d'abord les données Supabase dans une structure temporaire
                val supabasePlans = supabaseClient
                    .postgrest["subscription_plans"]
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

                Log.d("DEBUG", "Taille totale reçue : ${plans.size}")
                plans.forEach {
                    Log.d("DEBUG", "📦 ${it.name} - ${it.pricePerMonth}€ - ${it.type}")
                }

                runOnUiThread {
                    onResult(plans)
                }

            } catch (e: Exception) {
                Log.e("SUPABASE", "Erreur fetch abonnements: ${e.message}", e)
            }
        }
    }
}

@Serializable
data class Skate(
    val id: String,
    val serial_number: String,
    val model: String,
    val status: String,
    val created_at: String,
    val coordonnees: Map<String, Double>? = null
)

// Modèle pour la désérialisation des données de Supabase
@Serializable
data class SubscriptionPlanDto(
    val id: String,
    val name: String,
    val description: String?,
    val price: Double,
    val period: String,
    val type: String,
    val features: List<String>,
    val created_at: String
)

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
fun AppNavigation(supabaseClient: SupabaseClient) {
    val navController = rememberNavController()
    
    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                supabaseClient = supabaseClient,
                onLoginSuccess = {
                    navController.navigate("home") {
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
                    navController.navigate("login")
                },
                onLoginClick = {
                    navController.popBackStack()
                }
            )
        }
        
        composable("home") {
            HomeScreen(
                supabaseClient = supabaseClient,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}

