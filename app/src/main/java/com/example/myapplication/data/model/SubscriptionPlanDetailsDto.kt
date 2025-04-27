package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlanDetailsDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val price: Double,
    val period: String,
    val type: String,
    val features: List<String> = emptyList(),
    val created_at: String? = null
) 