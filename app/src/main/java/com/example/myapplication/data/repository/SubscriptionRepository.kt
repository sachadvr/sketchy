package com.example.myapplication.data.repository

import android.util.Log
import com.example.myapplication.data.model.SubscriptionPlan
import com.example.myapplication.data.model.SubscriptionType
import com.example.myapplication.data.model.UserSubscriptionDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data class SubscriptionPlanDto(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val period: String,
    val type: String,
    val features: List<String>? = null,
    val created_at: String? = null
)

class SubscriptionRepository @Inject constructor(
    private val supabaseClient: SupabaseClient
) {
    fun getSubscriptionPlansFlow(): Flow<List<SubscriptionPlan>> = flow {
        try {
            val plans = supabaseClient.postgrest["subscription_plans"]
                .select()
                .decodeList<SubscriptionPlanDto>()
                .map { dto ->
                    SubscriptionPlan(
                        id = dto.id,
                        name = dto.name,
                        description = dto.description,
                        pricePerMonth = dto.price,
                        type = when (dto.type.uppercase()) {
                            "BASIC" -> SubscriptionType.BASIC
                            "PREMIUM" -> SubscriptionType.PREMIUM
                            "UNLIMITED" -> SubscriptionType.UNLIMITED
                            else -> SubscriptionType.BASIC
                        },
                        features = dto.features ?: emptyList()
                    )
                }
            
            Log.d("SubscriptionRepository", "Plans d'abonnement chargés: ${plans.size}")
            emit(plans)
        } catch (e: Exception) {
            Log.e("SubscriptionRepository", "Erreur lors du chargement des plans d'abonnement", e)
            emit(emptyList<SubscriptionPlan>())
        }
    }

    suspend fun getUserActiveSubscriptionAsync(userId: String): UserSubscriptionDto? {
        try {
            val subscription = supabaseClient.postgrest["user_subscriptions"]
                .select {
                    filter {
                        eq("user_id", userId)
                        eq("status", "active")
                    }
                    order("start_date", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(1)
                }
                .decodeList<UserSubscriptionDto>()
                .firstOrNull()
            
            Log.d("SubscriptionRepository", "Abonnement actif trouvé: ${subscription?.id ?: "aucun"}")
            return subscription
        } catch (e: Exception) {
            Log.e("SubscriptionRepository", "Erreur lors de la recherche d'abonnement actif", e)
            return null
        }
    }

    suspend fun subscribeAsync(userId: String, planId: String): Boolean {
        try {
            val subscription = UserSubscriptionDto(
                id = java.util.UUID.randomUUID().toString(),
                user_id = userId,
                plan_id = planId,
                start_date = java.time.format.DateTimeFormatter.ISO_INSTANT.format(
                    java.time.Instant.now()
                ),
                end_date = java.time.format.DateTimeFormatter.ISO_INSTANT.format(
                    java.time.Instant.now().plusSeconds(30L * 24 * 60 * 60) 
                ),
                status = "active"
            )
            
            supabaseClient.postgrest["user_subscriptions"].insert(subscription)
            Log.d("SubscriptionRepository", "Abonnement créé: ${subscription.id}")
            return true
        } catch (e: Exception) {
            Log.e("SubscriptionRepository", "Erreur lors de la création d'abonnement", e)
            return false
        }
    }

    suspend fun cancelSubscriptionAsync(subscriptionId: String): Boolean {
        try {
            supabaseClient.postgrest["user_subscriptions"]
                .update({
                    set("status", "cancelled")
                    set("end_date", java.time.format.DateTimeFormatter.ISO_INSTANT.format(
                        java.time.Instant.now()
                    ))
                }) {
                    filter {
                        eq("id", subscriptionId)
                    }
                }
            Log.d("SubscriptionRepository", "Abonnement annulé: $subscriptionId")
            return true
        } catch (e: Exception) {
            Log.e("SubscriptionRepository", "Erreur lors de l'annulation d'abonnement", e)
            return false
        }
    }
} 