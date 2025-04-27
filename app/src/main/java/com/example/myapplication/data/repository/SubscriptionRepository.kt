package com.example.myapplication.data.repository

import com.example.myapplication.data.model.SubscriptionPlan
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SubscriptionRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun getSubscriptionPlansFlow(): Flow<List<SubscriptionPlan>> = flow {
        val plans = supabaseClient.postgrest["subscription_plans"]
            .select()
            .decodeList<SubscriptionPlan>()
        emit(plans)
    }

    suspend fun subscribeUserAsync(userId: String, planId: String) {
        supabaseClient.postgrest["user_subscriptions"].insert(
            mapOf(
                "user_id" to userId,
                "plan_id" to planId,
                "active" to true
            )
        )
    }

    suspend fun getUserSubscriptionAsync(userId: String): SubscriptionPlan? {
        return supabaseClient.postgrest["user_subscriptions"]
            .select {
                filter { 
                    eq("user_id", userId)
                    eq("active", true)
                }
            }
            .decodeList<SubscriptionPlan>()
            .firstOrNull()
    }
} 