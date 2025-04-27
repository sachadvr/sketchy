package com.example.myapplication

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime

object SupabaseManager {
    lateinit var client: SupabaseClient
        private set

    fun init(context: Context) {
        if (!::client.isInitialized) {
            val provider = SupabaseClientProvider(context)
            client = provider.initialize()
        }
    }
}
