package com.michaelliu.flightfx.data.remote

import com.michaelliu.flightfx.data.remote.api.FlightApi
import com.michaelliu.flightfx.data.remote.dto.FlightDto
import com.michaelliu.flightfx.domain.model.FlightCategory
import javax.inject.Inject

class RealFlightRemoteDataSource @Inject constructor(
    private val api: FlightApi,
) : FlightRemoteDataSource {
    override suspend fun getFlights(category: FlightCategory): List<FlightDto> =
        api.getSchedule(category.fileCode)
}
