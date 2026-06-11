package com.michaelliu.flightfx.data.remote

import com.michaelliu.flightfx.data.remote.dto.FlightDto
import com.michaelliu.flightfx.domain.model.FlightCategory

interface FlightRemoteDataSource {
    suspend fun getFlights(category: FlightCategory): List<FlightDto>
}
