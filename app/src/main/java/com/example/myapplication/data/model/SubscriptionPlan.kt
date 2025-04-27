package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String,
    val pricePerMonth: Double,
    val features: List<String>,
    val maxRidesPerMonth: Int? = null,
    val discountPercentage: Int = 0,
    val type: SubscriptionType = SubscriptionType.BASIC
)

enum class SubscriptionType {
    BASIC,
    PREMIUM,
    UNLIMITED
} 