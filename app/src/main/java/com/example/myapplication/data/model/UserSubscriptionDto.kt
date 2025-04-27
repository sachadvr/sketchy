package com.example.myapplication.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class UserSubscriptionDto(
    val id: String,
    val user_id: String,
    val plan_id: String,
    val start_date: String, 
    val end_date: String? = null, 
    val status: String,
    val created_at: String? = null
)


val SupabaseJson = Json { 
    ignoreUnknownKeys = true 
    isLenient = true
} 