package com.michaelliu.flightfx.data.repository

import com.michaelliu.flightfx.data.mapper.toFlight
import com.michaelliu.flightfx.data.remote.FlightRemoteDataSource
import com.michaelliu.flightfx.domain.model.Flight
import com.michaelliu.flightfx.domain.model.FlightCategory
import com.michaelliu.flightfx.domain.repository.FlightRepository
import com.michaelliu.flightfx.util.AppResult
import javax.inject.Inject

class FlightRepositoryImpl @Inject constructor(
    private val remoteDataSource: FlightRemoteDataSource,
) : FlightRepository {

    override suspend fun getFlights(category: FlightCategory): AppResult<List<Flight>> =
        safeApiCall {
            remoteDataSource.getFlights(category).map { it.toFlight(category) }
        }
}
