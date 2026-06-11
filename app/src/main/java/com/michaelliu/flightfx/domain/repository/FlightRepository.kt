package com.michaelliu.flightfx.domain.repository

import com.michaelliu.flightfx.domain.model.Flight
import com.michaelliu.flightfx.domain.model.FlightCategory
import com.michaelliu.flightfx.util.AppResult

interface FlightRepository {
    /** 取指定類別的航班清單。 */
    suspend fun getFlights(category: FlightCategory): AppResult<List<Flight>>
}
