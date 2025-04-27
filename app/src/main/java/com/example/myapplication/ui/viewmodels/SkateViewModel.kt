package com.example.myapplication.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import java.util.UUID
import kotlin.random.Random

@HiltViewModel
class SkateViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
) : ViewModel() {

    private val _skatesFlow = MutableStateFlow<List<Skate>>(emptyList())
    val skatesFlow: StateFlow<List<Skate>> = _skatesFlow

    private var isRefreshing = false

    init {
        Log.d("SkateViewModel", "Initialisation du SkateViewModel")
        refreshSkates()
    }

    fun refreshSkates() {
        if (isRefreshing) {
            Log.d("SkateViewModel", "Rafraîchissement déjà en cours, ignoré")
            return
        }
        
        isRefreshing = true
        Log.d("SkateViewModel", "Début du rafraîchissement des skates")
        
        viewModelScope.launch {
            try {
                val skatesDto = supabaseClient.postgrest["skates"]
                    .select()
                    .decodeList<SkateDto>()
                
                val skates = skatesDto.map { it.toSkate() }
                Log.d("SkateViewModel", "Skates récupérés: ${skates.size}")
                _skatesFlow.value = skates
            } catch (e: Exception) {
                Log.e("SkateViewModel", "Erreur lors de la récupération des skates", e)
            } finally {
                isRefreshing = false
            }
        }
    }

    fun addFakeSkateLocally() {
        val currentSkates = _skatesFlow.value.toMutableList()
        
        val id = UUID.randomUUID().toString()
        // Générer des coordonnées aléatoires autour du centre de Lille
        val randomLat = 50.6292 + (Random.nextDouble() - 0.5) * 0.01
        val randomLon = 3.0573 + (Random.nextDouble() - 0.5) * 0.01
        
        val fakeSkate = Skate(
            id = id,
            serial_number = "FAKE-${id.substring(0, 8)}",
            model = "Xiaomi M365",
            status = SkateStatus.AVAILABLE.name,
            coordinates = Coordinates(
                latitude = randomLat,
                longitude = randomLon
            ),
            created_at = java.time.Instant.now().toString()
        )
        
        currentSkates.add(fakeSkate)
        _skatesFlow.value = currentSkates
        
        Log.d("SkateViewModel", "Skate fictif ajouté localement: ID=$id")
    }
    
    fun updateSkateStatus(skateId: String, newStatus: SkateStatus) {
        viewModelScope.launch {
            try {
                Log.d("SkateViewModel", "Mise à jour du statut du skate #$skateId à $newStatus")
                
                val statusString = newStatus.name.lowercase()
                
                supabaseClient.postgrest["skates"]
                    .update({
                        set("status", statusString)
                    }) {
                        filter { eq("id", skateId) }
                    }
                
                Log.d("SkateViewModel", "Statut du skate #$skateId mis à jour avec succès")
                refreshSkates()
            } catch (e: Exception) {
                Log.e("SkateViewModel", "Erreur lors de la mise à jour du statut du skate #$skateId", e)
            }
        }
    }
} 