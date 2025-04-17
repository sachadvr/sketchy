package com.example.myapplication

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.GoTrue
import io.github.jan.supabase.gotrue.gotrue
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest

object SupabaseManager {

    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = "https://ktgjvogdwtcivzxabapo.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imt0Z2p2b2dkd3RjaXZ6eGFiYXBvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDM1MTgxNDQsImV4cCI6MjA1OTA5NDE0NH0.EwgFfvFa35WTQRk7znvLb3fRHcw3wfKzPVafViJn3r4"
    ) {
        install(GoTrue) {
            // config optionnel ici
        }
        install(Postgrest)
    }
}
