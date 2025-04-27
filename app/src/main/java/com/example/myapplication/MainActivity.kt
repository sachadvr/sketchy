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
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import io.github.jan.supabase.postgrest.postgrest
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.runtime.*
import com.example.myapplication.ui.components.AppContent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import io.github.jan.supabase.SupabaseClient

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

        // Récupère ton rideHistory, mapView, etc. ici
        val rides: List<RideHistoryItem> = rideHistory.getRides()

        findViewById<androidx.compose.ui.platform.ComposeView>(R.id.composeView).setContent {
            AppContent(
                supabaseClient = supabaseClient,
                rideHistory = rides,
                onRideClick = { ride -> showRideDetails(ride) },
                onLogout = { finish() },
                onDeleteAccount = { finish() },
                onStartRide = { location -> showStartRideConfirmation(location) },
                onEndRide = { showEndRideConfirmation() },
                elapsedTime = elapsedTime
            )
        }

        val mapView = findViewById<MapView>(R.id.mapView)
        mapView.setMultiTouchControls(true)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST)
        }

        testFetchSkates(mapView)

        findViewById<androidx.compose.ui.platform.ComposeView>(R.id.composeView).setContent {
            var subscriptionPlans = listOf<SubscriptionPlan>()

            LaunchedEffect(Unit) {
                fetchSubscriptionPlans { plans ->
                    subscriptionPlans = plans
                }
            }
            MainScreen(
                supabaseClient = supabaseClient,
                isRideActive = isRideActive,
                onStartRide = { location -> showStartRideConfirmation(location) },
                onEndRide = { showEndRideConfirmation() },
                elapsedTime = elapsedTime,
                onLogout = {
                    finish()
                },
                onDeleteAccount = {
                    finish()
                },
                rideHistory = rideHistory.getRides(),
                onRideClick = { ride -> showRideDetails(ride) },
                subscriptionPlans = subscriptionPlans
            )
        }

        startTime = SystemClock.elapsedRealtime()
        Thread {
            while (true) {
                if (isRideActive) {
                    elapsedTime = SystemClock.elapsedRealtime() - startTime
                    // Ici, vous devriez ajouter la position actuelle au chemin
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
            distance = calculateDistance(currentPath), // À implémenter
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
                val result = supabaseClient
                    .postgrest["subscription_plans"]
                    .select()
                    .decodeList<SubscriptionPlan>()

                Log.d("DEBUG", "Taille totale reçue : ${result.size}")
                result.forEach {
                    Log.d("DEBUG", "📦 ${it.name} - ${it.price}€ - ${it.type}")
                }

                runOnUiThread {
                    onResult(result)
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

@Serializable
data class SubscriptionPlan(
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

