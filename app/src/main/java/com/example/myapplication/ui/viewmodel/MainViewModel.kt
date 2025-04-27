package com.example.myapplication.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.GeoPointSerializable
import com.example.myapplication.data.model.RideHistoryItem
import com.example.myapplication.data.model.Skate
import com.example.myapplication.data.model.SubscriptionPlan
import com.example.myapplication.data.repository.RideRepository
import com.example.myapplication.data.repository.SkateRepository
import com.example.myapplication.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val rideRepository: RideRepository,
    private val skateRepository: SkateRepository,
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadRideHistory()
        loadAvailableSkates()
        loadSubscriptionPlans()
    }

    private fun loadRideHistory() {
        viewModelScope.launch {
            try {
                rideRepository.getRidesFlow().collectLatest { rides ->
                    _uiState.value = _uiState.value.copy(
                        rideHistory = rides,
                        error = null
                    )
                    Log.d("MainViewModel", "Historique de courses chargé: ${rides.size} courses")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erreur lors du chargement de l'historique", e)
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors du chargement de l'historique: ${e.message}"
                )
            }
        }
    }

    private fun loadAvailableSkates() {
        viewModelScope.launch {
            try {
                skateRepository.getAvailableSkatesFlow().collectLatest { skates ->
                    _uiState.value = _uiState.value.copy(
                        availableSkates = skates,
                        error = null
                    )
                    Log.d("MainViewModel", "Skates disponibles chargés: ${skates.size} skates")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erreur lors du chargement des skates", e)
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors du chargement des skates: ${e.message}"
                )
            }
        }
    }

    private fun loadSubscriptionPlans() {
        viewModelScope.launch {
            try {
                subscriptionRepository.getSubscriptionPlansFlow().collectLatest { plans ->
                    _uiState.value = _uiState.value.copy(
                        subscriptionPlans = plans,
                        error = null
                    )
                    Log.d("MainViewModel", "Plans d'abonnement chargés: ${plans.size} plans")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erreur lors du chargement des abonnements", e)
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors du chargement des abonnements: ${e.message}"
                )
            }
        }
    }

    fun startRide(skateId: String, location: GeoPoint) {
        val currentUserId = skateRepository.getCurrentUserId()
        if (currentUserId.isNullOrEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Vous devez être connecté pour démarrer une course"
            )
            return
        }
        
        
        
        
        val effectiveSkateId = if (skateId == "selected_skate") {
            
            _uiState.value.availableSkates.firstOrNull()?.id ?: skateId
        } else {
            skateId
        }
        
        Log.d("MainViewModel", "Démarrage de la course avec skateId=$effectiveSkateId")

        val ride = RideHistoryItem(
            id = UUID.randomUUID().toString(),
            userId = currentUserId,
            skateId = effectiveSkateId,
            startTime = System.currentTimeMillis(),
            endTime = null,
            distance = 0.0,
            price = 0.0,
            path = listOf(GeoPointSerializable(location.latitude, location.longitude))
        )

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    currentRide = ride,
                    error = null
                )
                Log.d("MainViewModel", "Course démarrée: ${ride.id}")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erreur lors du démarrage de la course", e)
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors du démarrage de la course: ${e.message}"
                )
            }
        }
    }

    fun updateCurrentRidePath(location: GeoPoint) {
        _uiState.value.currentRide?.let { currentRide ->
            val updatedPath = currentRide.path.toMutableList()
            updatedPath.add(GeoPointSerializable(location.latitude, location.longitude))
            
            _uiState.value = _uiState.value.copy(
                currentRide = currentRide.copy(
                    path = updatedPath
                )
            )
        }
    }

    fun endRide() {
        viewModelScope.launch {
            _uiState.value.currentRide?.let { ride ->
                val endTime = System.currentTimeMillis()
                val durationMs = endTime - ride.startTime
                val durationMinutes = durationMs / (1000 * 60)
                
                
                var totalDistance = 0.0
                val path = ride.path
                
                Log.d("MainViewModel", "Calcul de la distance pour ${path.size} points du chemin")
                if (path.size < 2) {
                    Log.d("MainViewModel", "Attention: Le chemin ne contient pas assez de points pour calculer une distance (${path.size} points)")
                    
                    if (path.isNotEmpty()) {
                        val originalPoint = GeoPoint(path[0].latitude, path[0].longitude)
                        
                        val newPoint = GeoPoint(
                            path[0].latitude + 0.001, 
                            path[0].longitude + 0.001
                        )
                        totalDistance = originalPoint.distanceToAsDouble(newPoint) / 1000.0
                        Log.d("MainViewModel", "Distance fictive créée: $totalDistance km")
                    }
                } else {
                    for (i in 1 until path.size) {
                        val prev = GeoPoint(path[i-1].latitude, path[i-1].longitude)
                        val current = GeoPoint(path[i].latitude, path[i].longitude)
                        val segmentDistance = prev.distanceToAsDouble(current)
                        totalDistance += segmentDistance
                        Log.d("MainViewModel", "Segment $i: distance = ${segmentDistance}m, total cumulé = ${totalDistance}m")
                    }
                    totalDistance /= 1000 
                }
                
                
                val price = durationMinutes * 1.0 + totalDistance * 0.5
                
                Log.d("MainViewModel", "Fin de course - Durée: ${durationMinutes}min, Distance: ${totalDistance}km, Prix: ${price}€")
                
                val updatedRide = ride.copy(
                    endTime = endTime,
                    distance = totalDistance,
                    price = price
                )
                
                try {
                    rideRepository.saveRideAsync(updatedRide)
                    _uiState.value = _uiState.value.copy(
                        currentRide = null,
                        error = null
                    )
                    loadRideHistory()
                    Log.d("MainViewModel", "Course terminée et sauvegardée: ${ride.id}")
                } catch (e: Exception) {
                    Log.e("MainViewModel", "Erreur lors de la sauvegarde de la course", e)
                    _uiState.value = _uiState.value.copy(
                        error = "Erreur lors de la sauvegarde de la course: ${e.message}"
                    )
                }
            }
        }
    }

    fun deleteRide(rideId: String) {
        viewModelScope.launch {
            try {
                rideRepository.deleteRideAsync(rideId)
                loadRideHistory()
                _uiState.value = _uiState.value.copy(
                    error = null
                )
                Log.d("MainViewModel", "Course supprimée: $rideId")
            } catch (e: Exception) {
                Log.e("MainViewModel", "Erreur lors de la suppression de la course", e)
                _uiState.value = _uiState.value.copy(
                    error = "Erreur lors de la suppression de la course: ${e.message}"
                )
            }
        }
    }
}

data class MainUiState(
    val availableSkates: List<Skate> = emptyList(),
    val rideHistory: List<RideHistoryItem> = emptyList(),
    val subscriptionPlans: List<SubscriptionPlan> = emptyList(),
    val currentRide: RideHistoryItem? = null,
    val error: String? = null
) 