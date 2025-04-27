package com.example.myapplication.model

import kotlinx.serialization.Serializable

@Serializable
enum class RideStatus {
    WAITING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
} 