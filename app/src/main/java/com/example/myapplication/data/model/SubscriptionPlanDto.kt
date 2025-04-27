package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlanDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val period: String,
    val type: String,
    val features: List<String> = emptyList(),
    val created_at: String? = null
)

fun SubscriptionPlanDto.toSubscriptionPlan(): SubscriptionPlan {
    return SubscriptionPlan(
        id = id,
        name = name,
        description = description ?: "",
        pricePerMonth = price,
        type = when (type.uppercase()) {
            "PREMIUM" -> SubscriptionType.PREMIUM
            "UNLIMITED" -> SubscriptionType.UNLIMITED
            else -> SubscriptionType.BASIC
        },
        features = features
    )
} 