package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.SupabaseClientProvider
import com.example.myapplication.data.repository.RideRepository
import com.example.myapplication.data.repository.SkateRepository
import com.example.myapplication.data.repository.SubscriptionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.github.jan.supabase.SupabaseClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideSupabaseClient(@ApplicationContext context: Context): SupabaseClient {
        return SupabaseClientProvider(context).initialize()
    }

    @Provides
    @Singleton
    fun provideRideRepository(supabaseClient: SupabaseClient): RideRepository {
        return RideRepository(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideSkateRepository(supabaseClient: SupabaseClient): SkateRepository {
        return SkateRepository(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideSubscriptionRepository(supabaseClient: SupabaseClient): SubscriptionRepository {
        return SubscriptionRepository(supabaseClient)
    }

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
} 