package com.example.myapplication.data

import com.example.myapplication.ui.screens.SubscriptionItem

class SubscriptionManager {
    private val subscriptions = mutableListOf<SubscriptionItem>()

    fun subscribe(subscription: SubscriptionItem) {
        if (subscriptions.isNotEmpty()) {
            println("Vous avez déjà un abonnement en cours. Veuillez annuler votre abonnement actuel pour en souscrire un nouveau.")
            return
        }
        subscriptions.add(subscription)
        println("Abonnement ajouté : ${subscription.name}")
    }

    fun unsubscribe(subscription: SubscriptionItem) {
        if (subscriptions.contains(subscription)) {
            subscriptions.remove(subscription)
            println("Abonnement annulé : ${subscription.name}")
        } else {
            println("Pas abonné à ${subscription.name}")
        }
    }

    fun getSubscriptions(): List<SubscriptionItem> = subscriptions
}
