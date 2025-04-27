package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.model.RideHistoryItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@Serializable
data class RideDto(
    val id: String,
    val user_id: String?,
    val start_time: String, 
    val end_time: String? = null, 
    val distance: Double,
    val price: Double,
    val start_location: LocationPointDto? = null,
    val end_location: LocationPointDto? = null,
    val path: List<LocationPointDto> = emptyList(),
    val created_at: String? = null 
)

@Serializable
data class LocationPointDto(
    val lat: Double,
    val lon: Double
)

class RideRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    
    private val formatter = DateTimeFormatter.ISO_INSTANT

    suspend fun saveRideAsync(ride: RideHistoryItem) {
        try {
            val rideDto = RideDto(
                id = ride.id,
                user_id = ride.userId,
                start_time = formatTimestamp(ride.startTime),
                end_time = ride.endTime?.let { formatTimestamp(it) },
                distance = ride.distance,
                price = ride.price,
                start_location = ride.path.firstOrNull()?.let { point ->
                    LocationPointDto(lat = point.latitude, lon = point.longitude)
                },
                end_location = ride.path.lastOrNull()?.let { point ->
                    LocationPointDto(lat = point.latitude, lon = point.longitude)
                },
                path = ride.path.map { point ->
                    LocationPointDto(lat = point.latitude, lon = point.longitude)
                },
                created_at = formatTimestamp(System.currentTimeMillis())
            )
            
            supabaseClient.postgrest["rides"].insert(rideDto)
            Log.d("RideRepository", "Course sauvegardée avec succès: ${ride.id}")
        } catch (e: Exception) {
            Log.e("RideRepository", "Erreur lors de la sauvegarde de la course", e)
            throw e
        }
    }

    fun getRidesFlow(): Flow<List<RideHistoryItem>> = flow {
        try {
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            
            if (currentUserId == null) {
                Log.w("RideRepository", "Aucun utilisateur connecté")
                emit(emptyList<RideHistoryItem>())
                return@flow
            }
            
            Log.d("RideRepository", "Chargement des courses pour l'utilisateur: $currentUserId")
            
            val rides = supabaseClient.postgrest["rides"]
                .select {
                    filter {
                        eq("user_id", currentUserId)
                    }
                    order("start_time", Order.DESCENDING)
                }
                .decodeList<RideDto>()
                .map { dto ->
                    RideHistoryItem(
                        id = dto.id,
                        userId = dto.user_id ?: "",
                        skateId = "", 
                        startTime = parseTimestamp(dto.start_time),
                        endTime = dto.end_time?.let { parseTimestamp(it) },
                        distance = dto.distance,
                        price = dto.price,
                        path = buildPathFromDto(dto)
                    )
                }
            
            Log.d("RideRepository", "Courses chargées: ${rides.size}")
            emit(rides)
        } catch (e: Exception) {
            Log.e("RideRepository", "Erreur lors du chargement des courses", e)
            emit(emptyList<RideHistoryItem>())
        }
    }
    
    
    private fun formatTimestamp(timestamp: Long): String {
        return formatter.format(Instant.ofEpochMilli(timestamp))
    }
    
    
    private fun parseTimestamp(timestampString: String): Long {
        return try {
            Instant.parse(timestampString).toEpochMilli()
        } catch (e: Exception) {
            Log.e("RideRepository", "Erreur de parsing de timestamp: $timestampString", e)
            System.currentTimeMillis() 
        }
    }
    
    private fun buildPathFromDto(dto: RideDto): List<com.example.myapplication.data.model.GeoPointSerializable> {
        
        if (dto.path.isNotEmpty()) {
            return dto.path.map { point ->
                com.example.myapplication.data.model.GeoPointSerializable(
                    latitude = point.lat,
                    longitude = point.lon
                )
            }
        }
        
        
        val path = mutableListOf<com.example.myapplication.data.model.GeoPointSerializable>()
        
        dto.start_location?.let {
            path.add(com.example.myapplication.data.model.GeoPointSerializable(
                latitude = it.lat,
                longitude = it.lon
            ))
        }
        
        dto.end_location?.let {
            
            if (dto.start_location == null || 
                it.lat != dto.start_location.lat || 
                it.lon != dto.start_location.lon) {
                path.add(com.example.myapplication.data.model.GeoPointSerializable(
                    latitude = it.lat,
                    longitude = it.lon
                ))
            }
        }
        
        return path
    }

    suspend fun deleteRideAsync(rideId: String) {
        try {
            supabaseClient.postgrest["rides"]
                .delete {
                    filter { eq("id", rideId) }
                }
            Log.d("RideRepository", "Course supprimée: $rideId")
        } catch (e: Exception) {
            Log.e("RideRepository", "Erreur lors de la suppression de la course", e)
            throw e
        }
    }
} 