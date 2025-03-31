package com.example

class PricingCalculator {
    companion object {
        private const val ACTIVATION_COST = 1.0
        private const val COST_PER_MINUTE = 0.15
        private const val MINIMUM_MINUTES = 1

        fun calculatePrice(minutes: Int): Double {
            val effectiveMinutes = maxOf(minutes, MINIMUM_MINUTES)
            return ACTIVATION_COST + (effectiveMinutes * COST_PER_MINUTE)
        }

        fun formatPrice(price: Double): String {
            return String.format("%.2f€", price)
        }

        fun estimatePrice(minutes: Int): String {
            val price = calculatePrice(minutes)
            return formatPrice(price)
        }
    }
} 