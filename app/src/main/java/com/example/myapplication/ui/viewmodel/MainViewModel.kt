package com.example.myapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.GeoPointSerializable
import com.example.myapplication.data.model.RideHistoryItem
import com.example.myapplication.data.model.RideStatus
import com.example.myapplication.data.model.Skate
import com.example.myapplication.data.model.SubscriptionPlan
import com.example.myapplication.data.repository.RideRepository
import com.example.myapplication.data.repository.SkateRepository
import com.example.myapplication.data.repository.SubscriptionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
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
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Charger les skates disponibles
            skateRepository.getSkatesFlow().collect { skates ->
                _uiState.value = _uiState.value.copy(
                    availableSkates = skates
                )
            }
        }

        viewModelScope.launch {
            // Charger l'historique des courses
            rideRepository.getRidesFlow().collect { rides ->
                _uiState.value = _uiState.value.copy(
                    rideHistory = rides
                )
            }
        }

        viewModelScope.launch {
            // Charger les abonnements
            subscriptionRepository.getSubscriptionPlansFlow().collect { plans ->
                _uiState.value = _uiState.value.copy(
                    subscriptionPlans = plans
                )
            }
        }
    }

    fun startRide(skateId: String, location: GeoPoint) {
        viewModelScope.launch {
            val ride = RideHistoryItem(
                id = java.util.UUID.randomUUID().toString(),
                userId = "current_user_id", // À remplacer par l'ID réel de l'utilisateur
                skateId = skateId,
                startTime = System.currentTimeMillis(),
                path = listOf(GeoPointSerializable.fromGeoPoint(location))
            )
            rideRepository.saveRideAsync(ride)
            _uiState.value = _uiState.value.copy(
                currentRide = ride
            )
        }
    }

    fun endRide() {
        viewModelScope.launch {
            _uiState.value.currentRide?.let { ride ->
                val updatedRide = ride.copy(
                    endTime = System.currentTimeMillis(),
                    status = RideStatus.COMPLETED
                )
                rideRepository.saveRideAsync(updatedRide)
                _uiState.value = _uiState.value.copy(
                    currentRide = null
                )
            }
        }
    }

    fun updateSkateLocation(skateId: String, location: GeoPoint) {
        viewModelScope.launch {
            skateRepository.updateSkateLocationAsync(
                skateId = skateId,
                lat = location.latitude,
                lon = location.longitude
            )
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