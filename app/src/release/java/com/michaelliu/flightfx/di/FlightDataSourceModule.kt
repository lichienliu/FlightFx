package com.michaelliu.flightfx.di

import com.michaelliu.flightfx.data.remote.FlightRemoteDataSource
import com.michaelliu.flightfx.data.remote.RealFlightRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FlightDataSourceModule {
    @Binds
    @Singleton
    abstract fun bindFlightRemoteDataSource(impl: RealFlightRemoteDataSource): FlightRemoteDataSource
}
