package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.model.Coordinates
import com.example.myapplication.data.model.Skate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class SkateDto(
    val id: String,
    val serial_number: String,
    val model: String,
    val status: String,
    val coordinates: CoordinatesDto? = null,
    val created_at: String? = null
)

@Serializable
data class CoordinatesDto(
    val latitude: Double,
    val longitude: Double
)


fun SkateDto.toSkate(): Skate {
    return Skate(
        id = this.id,
        serial_number = this.serial_number,
        model = this.model,
        status = this.status,
        coordinates = this.coordinates?.let {
            Coordinates(
                latitude = it.latitude,
                longitude = it.longitude
            )
        },
        created_at = this.created_at
    )
}

class SkateRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun getCurrentUserId(): String? {
        return supabaseClient.auth.currentUserOrNull()?.id
    }

    fun getAvailableSkatesFlow(): Flow<List<Skate>> = flow {
        try {
            val skates = supabaseClient.postgrest["skates"]
                .select {
                    filter {
                        eq("status", "available")
                    }
                }
                .decodeList<SkateDto>()
                .map { dto ->
                    dto.toSkate()
                }
            
            Log.d("SkateRepository", "Skates disponibles chargés: ${skates.size}")
            emit(skates)
        } catch (e: Exception) {
            Log.e("SkateRepository", "Erreur lors du chargement des skates", e)
            emit(emptyList<Skate>())
        }
    }

    suspend fun updateSkateStatusAsync(skateId: String, status: String) {
        try {
            supabaseClient.postgrest["skates"]
                .update({
                    set("status", status)
                }) {
                    filter {
                        eq("id", skateId)
                    }
                }
            Log.d("SkateRepository", "Statut du skate mis à jour: $skateId -> $status")
        } catch (e: Exception) {
            Log.e("SkateRepository", "Erreur lors de la mise à jour du statut du skate", e)
            throw e
        }
    }
    
    suspend fun updateSkateLocationAsync(skateId: String, lat: Double, lon: Double) {
        try {
            supabaseClient.postgrest["skates"]
                .update({
                    set("coordinates", mapOf(
                        "latitude" to lat,
                        "longitude" to lon
                    ))
                }) {
                    filter {
                        eq("id", skateId)
                    }
                }
            Log.d("SkateRepository", "Position du skate mise à jour: $skateId -> ($lat, $lon)")
        } catch (e: Exception) {
            Log.e("SkateRepository", "Erreur lors de la mise à jour de la position du skate", e)
            throw e
        }
    }
} 