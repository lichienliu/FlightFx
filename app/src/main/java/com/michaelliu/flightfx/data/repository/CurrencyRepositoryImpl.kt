package com.michaelliu.flightfx.data.repository

import com.michaelliu.flightfx.data.mapper.toExchangeRates
import com.michaelliu.flightfx.data.remote.api.CurrencyApi
import com.michaelliu.flightfx.domain.model.Currency
import com.michaelliu.flightfx.domain.model.ExchangeRates
import com.michaelliu.flightfx.domain.repository.CurrencyRepository
import com.michaelliu.flightfx.util.AppError
import com.michaelliu.flightfx.util.AppResult
import javax.inject.Inject

class CurrencyRepositoryImpl @Inject constructor(
    private val api: CurrencyApi,
) : CurrencyRepository {

    private var cached: ExchangeRates? = null

    // 啟動抓一次後快取;六幣別齊全且為有限正數才算成功(否則回 Parse、不快取)
    override suspend fun getLatestRates(): AppResult<ExchangeRates> {
        cached?.let { return AppResult.Success(it) }
        val result = safeApiCall {
            api.getLatestRates(
                baseCurrency = Currency.USD.name,
                currencies = SUPPORTED_CURRENCIES,
            ).toExchangeRates()
        }
        if (result is AppResult.Success && !result.data.isComplete()) {
            return AppResult.Failure(AppError.Parse())
        }
        return result.also { if (it is AppResult.Success) cached = it.data }
    }

    private fun ExchangeRates.isComplete(): Boolean =
        Currency.entries.all { rates[it]?.let { rate -> rate.isFinite() && rate > 0.0 } == true }

    private companion object {
        val SUPPORTED_CURRENCIES = Currency.entries.joinToString(",") { it.name }
    }
}
