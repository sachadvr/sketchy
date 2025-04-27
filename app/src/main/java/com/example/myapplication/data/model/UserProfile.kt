package com.example.myapplication.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val username: String? = null,
    val avatar_url: String? = null,
    val updated_at: String? = null,
    val created_at: String? = null
) 