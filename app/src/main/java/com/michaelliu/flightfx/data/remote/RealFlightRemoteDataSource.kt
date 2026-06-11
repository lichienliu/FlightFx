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

// 代號填進 FlightApi 的 InstantSchedule_{category}.json
private val FlightCategory.fileCode: String
    get() = when (this) {
        FlightCategory.INT_DEP -> "INTDEP"
        FlightCategory.INT_ARR -> "INTARR"
        FlightCategory.DOM_DEP -> "DOMDEP"
        FlightCategory.DOM_ARR -> "DOMARR"
    }
