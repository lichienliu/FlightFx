package com.michaelliu.flightfx.data.remote.api

import com.michaelliu.flightfx.data.remote.dto.LatestRatesDto
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyApi {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("base_currency") baseCurrency: String,
        @Query("currencies") currencies: String,
    ): LatestRatesDto
}
