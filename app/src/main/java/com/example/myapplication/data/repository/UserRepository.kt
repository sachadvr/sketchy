package com.example.myapplication.data.repository

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import com.example.myapplication.data.model.UserSubscriptionDto
import android.util.Log
import io.github.jan.supabase.gotrue.auth

class UserRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    
    private val formatter = DateTimeFormatter.ISO_INSTANT
    
    suspend fun saveUserSubscriptionAsync(userId: String, planId: String) {
        val subscription = UserSubscriptionDto(
            id = java.util.UUID.randomUUID().toString(),
            user_id = userId,
            plan_id = planId,
            start_date = formatTimestamp(System.currentTimeMillis()),
            end_date = formatTimestamp(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)), 
            status = "active"
        )
        
        supabaseClient.postgrest["user_subscriptions"].insert(subscription)
    }
    
    fun getUserSubscriptionFlow(userId: String): Flow<UserSubscriptionDto?> = flow {
        val subscription = supabaseClient.postgrest["user_subscriptions"]
            .select {
                filter {
                    eq("user_id", userId)
                    eq("status", "active")
                }
                order("start_date", Order.DESCENDING)
                limit(1)
            }
            .decodeList<UserSubscriptionDto>()
            .firstOrNull()
            
        emit(subscription)
    }
    
    suspend fun cancelSubscriptionAsync(subscriptionId: String) {
        supabaseClient.postgrest["user_subscriptions"]
            .update({
                set("status", "cancelled")
                set("end_date", formatTimestamp(System.currentTimeMillis()))
            }) {
                filter {
                    eq("id", subscriptionId)
                }
            }
    }
    
    suspend fun deleteAccount(): Boolean {
        try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id ?: return false
            
            // 1. Annuler tous les abonnements actifs
            supabaseClient.postgrest["user_subscriptions"]
                .update({
                    set("status", "cancelled")
                    set("end_date", formatTimestamp(System.currentTimeMillis()))
                }) {
                    filter {
                        eq("user_id", userId)
                        eq("status", "active")
                    }
                }
            
            // 2. Supprimer les courses associées
            supabaseClient.postgrest["rides"]
                .delete {
                    filter { eq("user_id", userId) }
                }
            
            // 3. Supprimer le compte utilisateur
            supabaseClient.auth.signOut()
            
            return true
        } catch (e: Exception) {
            Log.e("UserRepository", "Erreur lors de la suppression du compte: ${e.message}", e)
            return false
        }
    }
    
    private fun formatTimestamp(timestamp: Long): String {
        return formatter.format(Instant.ofEpochMilli(timestamp))
    }
} 