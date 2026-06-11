package com.michaelliu.flightfx.di

import com.michaelliu.flightfx.data.remote.api.CurrencyApi
import com.michaelliu.flightfx.data.repository.CurrencyRepositoryImpl
import com.michaelliu.flightfx.domain.repository.CurrencyRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CurrencyModule {

    @Binds
    @Singleton
    abstract fun bindCurrencyRepository(impl: CurrencyRepositoryImpl): CurrencyRepository

    companion object {
        @Provides
        @Singleton
        fun provideCurrencyApi(@CurrencyRetrofit retrofit: Retrofit): CurrencyApi =
            retrofit.create(CurrencyApi::class.java)
    }
}
