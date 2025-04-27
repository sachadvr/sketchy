package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String,
    val pricePerMonth: Double,
    val type: SubscriptionType,
    val features: List<String> = emptyList(),
    val maxRidesPerMonth: Int = 0,
    val discountPercentage: Int = 0
)

@Serializable
enum class SubscriptionType {
    BASIC, PREMIUM, UNLIMITED
} 