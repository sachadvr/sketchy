package com.example.myapplication.ui.components

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
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
    var locationOverlay by remember { mutableStateOf<MyLocationNewOverlay?>(null) }
    var currentLocation by remember { mutableStateOf<GeoPoint?>(null) }
    var isFirstLocation by remember { mutableStateOf(true) }
    val markers = remember { mutableStateListOf<Marker>() }
    val random = remember { Random() }

    val lilleLocation = remember { GeoPoint(50.6292, 3.0573) } // FOR TESTS ONLY (Mehdi's Location)

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
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    controller.setZoom(15.0)
                    val matrixA = ColorMatrix()
                    matrixA.setSaturation(0.3f)
                    val matrixB = ColorMatrix()
                    matrixB.setScale(1.12f, 1.13f, 1.13f, 1.0f)
                    matrixA.setConcat(matrixB, matrixA)
                    val filter = ColorMatrixColorFilter(matrixA)
                    overlayManager.tilesOverlay.setColorFilter(filter)
                    setMultiTouchControls(true)

                    val rotationGestureOverlay = RotationGestureOverlay(this)
                    rotationGestureOverlay.isEnabled = true
                    overlays.add(rotationGestureOverlay)

                    val myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                    myLocationOverlay.enableMyLocation()
                    myLocationOverlay.setPersonHotspot(0f, 0f)

                    val bitmap = android.graphics.Bitmap.createBitmap(40, 40, android.graphics.Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(bitmap)
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.BLUE
                        style = android.graphics.Paint.Style.FILL
                        isAntiAlias = true
                    }
                    canvas.drawCircle(20f, 20f, 15f, paint)

                    val strokePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.WHITE
                        style = android.graphics.Paint.Style.STROKE
                        strokeWidth = 3f
                        isAntiAlias = true
                    }
                    canvas.drawCircle(20f, 20f, 15f, strokePaint)

                    myLocationOverlay.setPersonIcon(bitmap)
                    overlays.add(myLocationOverlay)

                    mapView = this
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { view ->
                if (markers.isEmpty() && currentLocation != null) {
                    val center = currentLocation!!
                    repeat(5) {
                        val lat = center.latitude + (random.nextDouble() - 0.5) * 0.005
                        val lon = center.longitude + (random.nextDouble() - 0.5) * 0.005
                        val location = GeoPoint(lat, lon)

                        val marker = Marker(view)
                        marker.position = location
                        marker.title = "Skateboard électrique disponible"

                        val path = mutableListOf<GeoPoint>()
                        var currentPoint = location
                        repeat(5) {
                            val newLat = currentPoint.latitude + (random.nextDouble() - 0.5) * 0.002
                            val newLon = currentPoint.longitude + (random.nextDouble() - 0.5) * 0.002
                            currentPoint = GeoPoint(newLat, newLon)
                            path.add(currentPoint)
                        }

                        marker.setOnMarkerClickListener { _, _ ->
                            if (!isRideActive) {
                                onStartRide(location)
                            }
                            true
                        }
                        markers.add(marker)
                        view.overlays.add(marker)

                        val polyline = Polyline().apply {
                            setPoints(path)
                            color = android.graphics.Color.GRAY
                            width = 3f
                        }
                        view.overlays.add(polyline)
                    }
                }

                if (isRideActive && currentPath.isNotEmpty()) {
                    view.overlays.removeAll { it is Polyline }

                    val polyline = Polyline().apply {
                        setPoints(currentPath)
                        color = android.graphics.Color.GREEN
                        width = 5f
                    }
                    view.overlays.add(polyline)
                }
            }
        )

        FloatingActionButton(
            onClick = {
                currentLocation?.let { location ->
                    mapView?.controller?.setCenter(location)
                }
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = MaterialTheme.shapes.medium
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Recentrer sur ma position"
            )
        }

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