package com.michaelliu.flightfx.domain.model

/** 一次匯率快照,值為每 1 USD 兌多少該幣別(含 USD=1)。 */
data class ExchangeRates(
    val rates: Map<Currency, Double>,
)
