package com.example.myapplication.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Random
import kotlinx.coroutines.delay

@Composable
fun MapScreen(
    isRideActive: Boolean,
    onStartRide: (GeoPoint) -> Unit,
    onEndRide: () -> Unit,
    elapsedTime: Long,
    currentPath: List<GeoPoint> = emptyList()
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var isFirstLocation by remember { mutableStateOf(true) }
    val markers = remember { mutableStateListOf<Marker>() }
    val random = remember { Random() }

    val lilleLocation = remember { GeoPoint(50.6292, 3.0573) }
    
    // Créer un marqueur avec un cercle vert et un skateboard
    val createSkateMarker = { view: MapView, location: GeoPoint ->
        // Obtenir le drawable du skateboard
        val skateDrawable = ResourcesCompat.getDrawable(context.resources, R.drawable.skate, null)
        
        // Créer un bitmap pour le fond vert circulaire
        val bitmapSize = 180
        val bitmap = Bitmap.createBitmap(bitmapSize, bitmapSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Dessiner le cercle vert
        val paint = Paint().apply {
            color = android.graphics.Color.parseColor("#9CEB6A") // Vert clair
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        canvas.drawCircle(bitmapSize/2f, bitmapSize/2f, bitmapSize/2f, paint)
        
        // Dessiner le skateboard au centre
        skateDrawable?.let {
            val skateWidth = it.intrinsicWidth
            val skateHeight = it.intrinsicHeight
            val scale = (bitmapSize * 0.6f) / skateWidth.coerceAtLeast(skateHeight)
            
            it.setBounds(
                (bitmapSize - skateWidth * scale).toInt() / 2,
                (bitmapSize - skateHeight * scale).toInt() / 2,
                (bitmapSize + skateWidth * scale).toInt() / 2,
                (bitmapSize + skateHeight * scale).toInt() / 2
            )
            it.draw(canvas)
        }
        
        // Créer un marker avec le bitmap personnalisé
        val marker = Marker(view).apply {
            position = location
            title = "Skateboard"
            icon = BitmapDrawable(context.resources, bitmap)
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            
            setOnMarkerClickListener { _, _ ->
                if (!isRideActive) {
                    onStartRide(location)
                }
                true
            }
        }
        
        marker
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    LaunchedEffect(mapView) {
        while (true) {
            mapView?.let { view ->
                val lat = lilleLocation.latitude + (random.nextDouble() - 0.5) * 0.001
                val lon = lilleLocation.longitude + (random.nextDouble() - 0.5) * 0.001
                val newLocation = GeoPoint(lat, lon)
                currentLocation = newLocation

                if (isFirstLocation) {
                    view.controller.setCenter(newLocation)
                    isFirstLocation = false
                }
            }
            delay(1000)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Carte en plein écran
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(15.0)
                    setMultiTouchControls(true)
                    
                    val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    myLocationOverlay.enableMyLocation()
                    overlays.add(myLocationOverlay)

                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                if (markers.isEmpty() && currentLocation != null) {
                    val center = currentLocation!!
                    
                    // Ajouter un seul marker de skate au centre
                    val marker = createSkateMarker(view, center)
                    markers.add(marker)
                    view.overlays.add(marker)
                    
                    view.invalidate()
                } else if (currentLocation != null && markers.isNotEmpty()) {
                    // Mettre à jour la position du marker pour qu'il suive la position actuelle
                    markers[0].position = currentLocation
                    view.invalidate()
                }

                if (isRideActive && currentPath.isNotEmpty()) {
                    // Afficher le tracé de la course en cours
                    view.overlays.removeAll { it is Polyline }

                    val polyline = Polyline().apply {
                        outlinePaint.color = android.graphics.Color.GREEN
                        outlinePaint.strokeWidth = 5f
                        setPoints(currentPath)
                    }
                    view.overlays.add(polyline)
                    
                    view.invalidate()
                }
            }
        )

        // Contrôles de la carte
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .zIndex(10f)
        ) {
            // Bouton zoom +
            FloatingActionButton(
                onClick = { mapView?.controller?.zoomIn() },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom in")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Bouton zoom -
            FloatingActionButton(
                onClick = { mapView?.controller?.zoomOut() },
                modifier = Modifier.size(40.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "Zoom out")
            }
        }

        // Bouton de localisation
        FloatingActionButton(
            onClick = {
                currentLocation?.let { location ->
                    mapView?.controller?.setCenter(location)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
                .zIndex(10f),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Ma position"
            )
        }

        // Interface de course active
        if (isRideActive) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .zIndex(10f),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = formatTime(elapsedTime),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = onEndRide,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text("Terminer la course")
                    }
                }
            }
        }
    }
}

private fun formatTime(elapsedMillis: Long): String {
    val minutes = elapsedMillis / 60000
    val seconds = (elapsedMillis % 60000) / 1000
    return String.format("%02d:%02d", minutes, seconds)
}