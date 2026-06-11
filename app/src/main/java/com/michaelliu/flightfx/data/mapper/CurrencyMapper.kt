package com.michaelliu.flightfx.data.mapper

import com.michaelliu.flightfx.data.remote.dto.LatestRatesDto
import com.michaelliu.flightfx.domain.model.Currency
import com.michaelliu.flightfx.domain.model.ExchangeRates

/** 只留 [Currency] 支援的六幣別,其餘忽略。 */
fun LatestRatesDto.toExchangeRates(): ExchangeRates {
    val rates = Currency.entries.mapNotNull { c -> data[c.name]?.let { c to it } }.toMap()
    return ExchangeRates(rates)
}
