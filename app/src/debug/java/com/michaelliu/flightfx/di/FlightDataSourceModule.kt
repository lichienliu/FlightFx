package com.michaelliu.flightfx.di

import com.michaelliu.flightfx.BuildConfig
import com.michaelliu.flightfx.data.remote.FakeFlightRemoteDataSource
import com.michaelliu.flightfx.data.remote.FlightRemoteDataSource
import com.michaelliu.flightfx.data.remote.RealFlightRemoteDataSource
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FlightDataSourceModule {

    @Provides
    @Singleton
    fun provideFlightRemoteDataSource(
        real: Lazy<RealFlightRemoteDataSource>,
        fake: Lazy<FakeFlightRemoteDataSource>,
    ): FlightRemoteDataSource =
        if (BuildConfig.USE_MOCK) fake.get() else real.get()
}
