package com.michaelliu.flightfx.di

import com.michaelliu.flightfx.data.remote.api.CurrencyApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CurrencyModule {

    @Provides
    @Singleton
    fun provideCurrencyApi(@CurrencyRetrofit retrofit: Retrofit): CurrencyApi =
        retrofit.create(CurrencyApi::class.java)
}
