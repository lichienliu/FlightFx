package com.michaelliu.flightfx.data.remote.api

import com.michaelliu.flightfx.data.remote.dto.FlightDto
import retrofit2.http.GET
import retrofit2.http.Path

interface FlightApi {
    @GET("InstantSchedule_{category}.json")
    suspend fun getSchedule(@Path("category") category: String): List<FlightDto>
}
