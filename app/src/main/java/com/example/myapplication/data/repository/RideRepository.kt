package com.example.myapplication.data.repository

import com.example.myapplication.data.model.RideHistoryItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class RideRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    suspend fun saveRideAsync(ride: RideHistoryItem) {
        supabaseClient.postgrest["rides"].insert(ride)
    }

    fun getRidesFlow(): Flow<List<RideHistoryItem>> = flow {
        val rides = supabaseClient.postgrest["rides"]
            .select()
            .decodeList<RideHistoryItem>()
        emit(rides)
    }

    suspend fun deleteRideAsync(rideId: String) {
        supabaseClient.postgrest["rides"]
            .delete {
                filter { eq("id", rideId) }
            }
    }
} 