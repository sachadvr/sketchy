package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.PricingCalculator
import com.example.myapplication.ui.components.MapScreen
import org.osmdroid.util.GeoPoint

class MainActivity : AppCompatActivity() {

    private var isRideActive by mutableStateOf(false)
    private var startTime by mutableStateOf(0L)
    private var elapsedTime by mutableStateOf(0L)
    private val LOCATION_PERMISSION_REQUEST = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST)
        }

        findViewById<androidx.compose.ui.platform.ComposeView>(R.id.composeView).setContent {
            MapScreen(
                isRideActive = isRideActive,
                onStartRide = { location -> showStartRideConfirmation(location) },
                onEndRide = { showEndRideConfirmation() },
                elapsedTime = elapsedTime
            )
        }

        startTime = SystemClock.elapsedRealtime()
        Thread {
            while (true) {
                if (isRideActive) {
                    elapsedTime = SystemClock.elapsedRealtime() - startTime
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
                startRide()
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun startRide() {
        isRideActive = true
        startTime = SystemClock.elapsedRealtime()
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
        val price = PricingCalculator.estimatePrice(minutes.toInt())
        
        AlertDialog.Builder(this)
            .setTitle("Course terminée ($price)")
            .setMessage("Durée de la course : ${String.format("%.1f", minutes)} minutes")
            .setPositiveButton("OK", null)
            .show()
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
} 