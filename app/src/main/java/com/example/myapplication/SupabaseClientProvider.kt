package com.example.myapplication

import android.content.Context
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.gotrue.auth
import com.example.myapplication.data.model.SupabaseJson
import javax.inject.Inject
import javax.inject.Singleton
import io.github.jan.supabase.serializer.KotlinXSerializer

@Singleton
class SupabaseClientProvider @Inject constructor(
    private val context: Context
) {
    private var client: SupabaseClient? = null

    fun initialize(): SupabaseClient {
        if (client == null) {
            client = createSupabaseClient(
                supabaseUrl = Config.SUPABASE_URL,
                supabaseKey = Config.SUPABASE_ANON_KEY,
            ) {
                install(Auth) {
                    
                    scheme = "app"
                    host = "supabase.com"
                    serializer = KotlinXSerializer(SupabaseJson)
                }
                install(Postgrest) {
                    serializer = KotlinXSerializer(SupabaseJson)
                }
                install(Realtime) {
                    serializer = KotlinXSerializer(SupabaseJson)
                }
            }
        }
        return client!!
    }
} 