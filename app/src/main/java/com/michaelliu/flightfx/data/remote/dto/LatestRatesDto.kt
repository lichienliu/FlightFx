package com.michaelliu.flightfx.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LatestRatesDto(
    /** 幣別代碼 → 每 1 USD 兌多少 */
    val data: Map<String, Double> = emptyMap(),
)
