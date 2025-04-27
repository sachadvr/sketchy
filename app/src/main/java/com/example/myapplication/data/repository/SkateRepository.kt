package com.example.myapplication.data.repository

import com.example.myapplication.data.model.Skate
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SkateRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun getSkatesFlow(): Flow<List<Skate>> = flow {
        val skates = supabaseClient.postgrest["skates"]
            .select()
            .decodeList<Skate>()
        emit(skates)
    }

    suspend fun updateSkateLocationAsync(skateId: String, lat: Double, lon: Double) {
        supabaseClient.postgrest["skates"]
            .update({
                set("coordonnees", mapOf(
                    "lat" to lat,
                    "lon" to lon
                ))
            }) {
                filter { eq("id", skateId) }
            }
    }

    suspend fun getAvailableSkatesAsync(): List<Skate> {
        return supabaseClient.postgrest["skates"]
            .select {
                filter { eq("disponible", true) }
            }
            .decodeList<Skate>()
    }
} 