package com.michaelliu.flightfx.di

import com.michaelliu.flightfx.data.remote.api.FlightApi
import com.michaelliu.flightfx.data.repository.FlightRepositoryImpl
import com.michaelliu.flightfx.domain.repository.FlightRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FlightModule {

    @Binds
    @Singleton
    abstract fun bindFlightRepository(impl: FlightRepositoryImpl): FlightRepository

    companion object {
        @Provides
        @Singleton
        fun provideFlightApi(@FlightRetrofit retrofit: Retrofit): FlightApi =
            retrofit.create(FlightApi::class.java)
    }
}
