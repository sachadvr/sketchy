package com.example.myapplication.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.R
import com.example.myapplication.data.model.RideHistoryItem
import com.example.myapplication.data.model.Skate
import com.example.myapplication.data.model.Coordinates
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.CopyrightOverlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.MinimapOverlay
import android.graphics.Color
import org.osmdroid.config.Configuration
import org.osmdroid.views.CustomZoomButtonsController

private fun drawableToIcon(drawable: Drawable?): Bitmap? {
    if (drawable == null) return null
    
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    
    return bitmap
}

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
            Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            
            zoomController.setVisibility(CustomZoomButtonsController.Visibility.ALWAYS)
            
            controller.setCenter(GeoPoint(50.62925, 3.057256))
        }
    }

    DisposableEffect(skates, currentRide) {
        
        mapView.overlays.clear()

        
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), mapView).apply {
            enableMyLocation()
            enableFollowLocation()
            val locationIcon = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_location, null)
            setPersonIcon(drawableToIcon(locationIcon))
        }
        mapView.overlays.add(locationOverlay)

        
        val compassOverlay = CompassOverlay(context, mapView).apply {
            enableCompass()
        }
        mapView.overlays.add(compassOverlay)

        
        val scaleBarOverlay = ScaleBarOverlay(mapView).apply {
            setAlignBottom(true)
            setAlignRight(true)
        }
        mapView.overlays.add(scaleBarOverlay)

        
        
        skates.forEach { skate ->
            val coords = skate.coordinates ?: Coordinates(50.62925, 3.057256)
            val marker = Marker(mapView).apply {
                position = GeoPoint(coords.latitude, coords.longitude)
                title = "Skate #${skate.id}"
                snippet = "Modèle: ${skate.model}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                
                
                val iconDrawable = ResourcesCompat.getDrawable(mapView.resources, R.drawable.skate, null)
                icon = iconDrawable
                
                
                setOnMarkerClickListener { marker, _ ->
                    onSkateClick(skate, position)
                    true  
                }
            }
            
            mapView.overlays.add(marker)
        }

        
        currentRide?.let { ride ->
            
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