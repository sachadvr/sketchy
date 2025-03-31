package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.Random

@Composable
fun MapScreen(
    isRideActive: Boolean,
    onStartRide: (GeoPoint) -> Unit,
    onEndRide: () -> Unit,
    elapsedTime: Long
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    val markers = remember { mutableStateListOf<Marker>() }
    val random = remember { Random() }

    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", 0))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(15.0)
                    setMultiTouchControls(true)
                    
                    val rotationGestureOverlay = RotationGestureOverlay(this)
                    rotationGestureOverlay.isEnabled = true
                    overlays.add(rotationGestureOverlay)
                    
                    locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    locationOverlay?.enableMyLocation()
                    overlays.add(locationOverlay)
                    
                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                if (markers.isEmpty()) {
                    repeat(5) {
                        val lat = 48.8566 + (random.nextDouble() - 0.5) * 0.02
                        val lon = 2.3522 + (random.nextDouble() - 0.5) * 0.02
                        val location = GeoPoint(lat, lon)
                        
                        val marker = Marker(view)
                        marker.position = location
                        marker.title = "Skateboard électrique disponible"
                        marker.setOnMarkerClickListener { _, _ ->
                            if (!isRideActive) {
                                onStartRide(location)
                            }
                            true
                        }
                        markers.add(marker)
                        view.overlays.add(marker)
                    }
                }
            }
        )

        if (isRideActive) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
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
                        )
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