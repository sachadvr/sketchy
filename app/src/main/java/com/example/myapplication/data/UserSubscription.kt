package com.example.myapplication.data

import kotlinx.serialization.Serializable

@Serializable
data class UserSubscription(
    val user_id: String,
    val subscription_plan_id: String,
    val status: String = "active",
    val start_date: String,
    val end_date: String? = null
)
