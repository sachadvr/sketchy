package com.example.myapplication.ui.components

import android.content.Context
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.data.model.RideHistoryItem
import com.example.myapplication.data.model.Skate
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    skates: List<Skate>,
    currentRide: RideHistoryItem?,
    onSkateClick: (Skate, GeoPoint) -> Unit
) {
    val context = LocalContext.current
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            // Centrer sur Lille par défaut
            controller.setCenter(GeoPoint(50.62925, 3.057256))
        }
    }

    DisposableEffect(skates, currentRide) {
        // Configurer la carte
        mapView.overlays.clear()

        // Ajouter les marqueurs pour chaque skate
        skates.forEach { skate ->
            skate.coordonnees?.let { coords ->
                val point = GeoPoint(coords["lat"] ?: 0.0, coords["lon"] ?: 0.0)
                val marker = Marker(mapView).apply {
                    position = point
                    title = "Skate ${skate.model}"
                    snippet = "Batterie: ${skate.batteryLevel}%"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    setOnMarkerClickListener { _, _ ->
                        onSkateClick(skate, point)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
        }

        // Ajouter le tracé de la course en cours
        currentRide?.let { ride ->
            // TODO: Ajouter une Polyline pour le tracé de la course
        }

        mapView.invalidate()

        onDispose {
            mapView.onDetach()
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            view.invalidate()
        }
    )
} 