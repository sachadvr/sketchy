package com.example.myapplication.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlinx.coroutines.delay
import kotlin.random.Random
import com.example.myapplication.data.model.Skate
import com.example.myapplication.data.model.SkateStatus
import android.graphics.Color as AndroidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myapplication.ui.viewmodels.SkateViewModel
import android.widget.Toast
import com.example.myapplication.data.model.Coordinates
import androidx.compose.material.icons.filled.Add
import com.example.myapplication.ui.viewmodel.MainViewModel

private const val IS_DEV_MODE = true 
private const val LILLE_CENTER_LAT = 50.6292
private const val LILLE_CENTER_LON = 3.0573
private const val MOCK_LOCATION_RADIUS = 0.002 

class MockLocationProvider(context: Context) : GpsMyLocationProvider(context) {
    private val random = Random(System.currentTimeMillis())
    private var currentLat = LILLE_CENTER_LAT
    private var currentLon = LILLE_CENTER_LON
    private var lastUpdateTime = 0L
    private val UPDATE_INTERVAL = 3000L 

    init {
        Log.d("MockLocationProvider", "Initialized with Lille center position: $LILLE_CENTER_LAT, $LILLE_CENTER_LON")
    }

    override fun getLastKnownLocation(): android.location.Location {
        val currentTime = System.currentTimeMillis()
        
        
        if (currentTime - lastUpdateTime > UPDATE_INTERVAL) {
            if (IS_DEV_MODE) {
                
                currentLat = LILLE_CENTER_LAT + (random.nextDouble() - 0.5) * MOCK_LOCATION_RADIUS
                currentLon = LILLE_CENTER_LON + (random.nextDouble() - 0.5) * MOCK_LOCATION_RADIUS
                Log.d("MockLocationProvider", "Updated mock location to: $currentLat, $currentLon")
            }
            lastUpdateTime = currentTime
        }

        return android.location.Location("mock").apply {
            latitude = currentLat
            longitude = currentLon
            accuracy = 10f
            time = currentTime
            speed = 1.5f 
            bearing = random.nextFloat() * 360
            elapsedRealtimeNanos = System.nanoTime()
        }
    }
}

private fun addSkateMarker(
    context: Context,
    map: MapView,
    skate: Skate,
    markerColor: Color,
    onMarkerClick: (Skate) -> Unit
): Marker {
    val marker = Marker(map)
    val coords = skate.coordinates ?: Coordinates(50.62925, 3.057256)
    marker.position = GeoPoint(coords.latitude, coords.longitude)
    
    
    val bitmap = buildSkateIcon(context, markerColor)
    marker.icon = BitmapDrawable(context.resources, bitmap)
    
    
    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
    
    
    marker.title = "Skate #${skate.id}"
    marker.snippet = "Modèle: ${skate.model}\nStatus: ${skate.status}"
    
    
    if (skate.status.uppercase() == "AVAILABLE") {
        marker.showInfoWindow()
    }
    
    marker.setOnMarkerClickListener { clickedMarker, _ ->
        Log.d("MapScreen", "Marqueur cliqué: ${skate.id}")
        onMarkerClick(skate)
        true
    }
    
    return marker
}



private fun buildSkateIcon(context: Context, color: Color): Bitmap {
    val size = 60  
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paint = Paint().apply {
        this.color = color.toArgb()
        style = Paint.Style.FILL
        isAntiAlias = true
    }
    
    canvas.drawCircle(size/2f, size/2f, size/2f - 4f, paint)
    
    paint.apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        this.color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(size/2f, size/2f, size/2f - 4f, paint)
    
    paint.apply {
        strokeWidth = 2f
        this.color = android.graphics.Color.BLACK
    }
    canvas.drawCircle(size/2f, size/2f, size/2f - 2f, paint)
    
    return bitmap
}

@Composable
private fun RideActiveCard(
    elapsedTime: String,
    onEndRide: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("RideActiveCard", "Affichage du timer avec elapsedTime=$elapsedTime")
    Card(
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Temps écoulé",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = elapsedTime,
                style = MaterialTheme.typography.headlineMedium
            )
            Button(
                onClick = onEndRide,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Terminer la course")
            }
        }
    }
}

private fun getSkateColor(status: String): Int {
    return when (status) {
        "AVAILABLE" -> android.graphics.Color.BLUE
        "IN_USE" -> android.graphics.Color.RED
        "MAINTENANCE" -> android.graphics.Color.YELLOW
        "OUT_OF_SERVICE" -> android.graphics.Color.GRAY
        else -> android.graphics.Color.BLACK
    }
}

@Composable
private fun RideConfirmationDialog(
    skate: Skate,
    onConfirm: (Skate) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmation de location") },
        text = { 
            Column {
                Text(
                    "Voulez-vous commencer la location du Skate #${skate.id} ?",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Modèle: ${skate.model}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(skate) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Commencer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}

@Composable
fun MapScreen(
    modifier: Modifier = Modifier,
    isRideActive: Boolean = false,
    onStartRide: (GeoPoint) -> Unit = {},
    onEndRide: () -> Unit = {},
    elapsedTime: String = "00:00",
    availableSkates: List<Skate> = emptyList(),
    onSkateClick: (Skate) -> Unit = {},
    skateViewModel: SkateViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var myLocationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    val lilleCenterPoint = remember { GeoPoint(LILLE_CENTER_LAT, LILLE_CENTER_LON) }
    var isMapInitialized by remember { mutableStateOf(false) }
    
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var selectedSkate by remember { mutableStateOf<Skate?>(null) }
    
    val skates by skateViewModel.skatesFlow.collectAsState(initial = emptyList())
    
    val coroutineScope = rememberCoroutineScope()

    
    val materialThemePrimary = MaterialTheme.colorScheme.primary
    val materialThemeError = MaterialTheme.colorScheme.error
    val materialThemeTertiary = MaterialTheme.colorScheme.tertiary
    val materialThemeOnSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    
    
    val skateMarkers = remember(skates) {
        skates.map { skate ->
            val markerColor = when (skate.status.uppercase()) {
                "AVAILABLE" -> materialThemePrimary
                "IN_USE" -> materialThemeError
                "MAINTENANCE" -> materialThemeTertiary
                "OUT_OF_SERVICE" -> materialThemeOnSurfaceVariant
                else -> materialThemeOnSurfaceVariant
            }
            skate to markerColor
        }
    }

    DisposableEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        
        
        skateViewModel.refreshSkates()
        
        
        val job = coroutineScope.launch {
            while (isActive) {
                delay(10000) 
                Log.d("MapScreen", "Rafraîchissement automatique des skates")
                skateViewModel.refreshSkates()
            }
        }
        
        onDispose {
            mapView?.onDetach()
            job.cancel() 
        }
    }

    
    LaunchedEffect(Unit) {
        Log.d("MapScreen", "Initialisation de la carte centrée sur Lille")
    }

    
    LaunchedEffect(isRideActive) {
        Log.d("MapScreen", "État de la course modifié: $isRideActive, temps écoulé: $elapsedTime")
    }

    
    LaunchedEffect(isRideActive) {
        if (isRideActive) {
            Log.d("MapScreen", "Démarrage de la simulation de mouvement")
            
            while (isRideActive) {
                delay(5000) 
                
                val randomLat = LILLE_CENTER_LAT + (Random.nextDouble() - 0.5) * 0.002
                val randomLon = LILLE_CENTER_LON + (Random.nextDouble() - 0.5) * 0.002
                val newLocation = GeoPoint(randomLat, randomLon)
                
                Log.d("MapScreen", "Mise à jour de la position: $newLocation")
                
                mainViewModel.updateCurrentRidePath(newLocation)
            }
            Log.d("MapScreen", "Fin de la simulation de mouvement")
        }
    }

    
    val handleSkateClick: (Skate) -> Unit = { skate ->
        if (skate.status.uppercase() == "AVAILABLE" && !isRideActive) {
            Log.d("MapScreen", "Skate sélectionné pour démarrer une course: ${skate.id}")
            selectedSkate = skate
            showConfirmationDialog = true
        } else {
            
            when {
                isRideActive -> Toast.makeText(context, "Vous êtes déjà en course", Toast.LENGTH_SHORT).show()
                skate.status.uppercase() != "AVAILABLE" -> Toast.makeText(context, "Ce skate n'est pas disponible", Toast.LENGTH_SHORT).show()
            }
        }
    }

    
    val addSkateMarkers = { mapView: MapView ->
        
        mapView.overlays.clear()
        
        
        if (IS_DEV_MODE) {
            val centerLille = GeoPoint(LILLE_CENTER_LAT, LILLE_CENTER_LON)
            mapView.controller.animateTo(centerLille)
        }
        
        
        if (skateMarkers.isNotEmpty()) {
            Log.d("MapScreen", "Ajout de ${skateMarkers.size} marqueurs sur la carte")
        }
        
        
        skateMarkers.forEach { (skate, markerColor) ->
            val coords = skate.coordinates ?: Coordinates(50.62925, 3.057256)
            
            val marker = addSkateMarker(context, mapView, skate, markerColor, handleSkateClick)
            mapView.overlays.add(marker)
        }
        
        
        mapView.overlays.add(myLocationOverlay)
        mapView.invalidate()
    }

    
    LaunchedEffect(skates) {
        if (skates.isNotEmpty() && mapView != null) {
            Log.d("MapScreen", "Mise à jour des marqueurs suite à changement de skates (${skates.size} skates)")
            addSkateMarkers(mapView!!)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    
                    
                    
                    myLocationOverlay = MyLocationNewOverlay(MockLocationProvider(context), this).apply {
                        enableMyLocation()
                        
                        
                    }
                    overlays.add(myLocationOverlay)
                    
                    
                    controller.setCenter(lilleCenterPoint)
                    controller.setZoom(15.0)
                    isMapInitialized = true
                    Log.d("MapScreen", "Carte initialisée et centrée sur Lille")
                    
                    mapView = this
                    
                    
                    addSkateMarkers(this)
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                
                
                
                if (skates.isNotEmpty() && view.overlays.size <= 1) {
                    addSkateMarkers(view)
                }
            }
        )

        
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            
            FloatingActionButton(
                onClick = {
                    Log.d("MapScreen", "Ajout d'un skate fictif à Lille")
                    skateViewModel.addFakeSkateLocally()
                    Toast.makeText(
                        context,
                        "Nouveau skate fictif ajouté sur la carte",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ajouter un skate fictif"
                )
            }
            
            
            FloatingActionButton(
                onClick = {
                    Log.d("MapScreen", "Rafraîchissement manuel des skates")
                    skateViewModel.refreshSkates()
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh, 
                    contentDescription = "Rafraîchir les skates"
                )
            }
            
            
            FloatingActionButton(
                onClick = {
                    mapView?.controller?.animateTo(lilleCenterPoint)
                    mapView?.controller?.setZoom(15.0)
                    Log.d("MapScreen", "Recentrage manuel sur Lille")
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Centrer sur Lille"
                )
            }
        }

        if (isRideActive) {
            Log.d("MapScreen", "Affichage du timer: $elapsedTime (isRideActive=$isRideActive)")
            RideActiveCard(
                elapsedTime = elapsedTime,
                onEndRide = onEndRide,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )
        }
    }

    
    if (showConfirmationDialog && selectedSkate != null) {
        RideConfirmationDialog(
            skate = selectedSkate!!,
            onConfirm = { skate ->
                showConfirmationDialog = false
                
                val skateLocation = skate.coordinates?.latitude?.let { lat ->
                    skate.coordinates?.longitude?.let { lon ->
                        GeoPoint(lat, lon)
                    }
                } ?: lilleCenterPoint 
                
                
                skateViewModel.updateSkateStatus(skate.id, SkateStatus.IN_USE)
                
                
                Log.d("MapScreen", "Démarrage de la course avec le skate #${skate.id} à la position $skateLocation")
                onStartRide(skateLocation)
                
                
                
                
                val localIsRideActive = true
                if (localIsRideActive) {
                    Log.d("MapScreen", "Forçage de l'affichage du timer")
                    
                    
                }
                
                
                Toast.makeText(
                    context,
                    "Course démarrée avec le skate #${skate.id}",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onDismiss = {
                showConfirmationDialog = false
                selectedSkate = null
            }
        )
    }
}