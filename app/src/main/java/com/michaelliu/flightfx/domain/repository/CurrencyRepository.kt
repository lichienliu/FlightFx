package com.michaelliu.flightfx.domain.repository

import com.michaelliu.flightfx.domain.model.ExchangeRates
import com.michaelliu.flightfx.util.AppResult

interface CurrencyRepository {
    /** 取最新匯率(啟動抓一次後快取)。 */
    suspend fun getLatestRates(): AppResult<ExchangeRates>
}
