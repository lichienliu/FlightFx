package com.michaelliu.flightfx.ui.currency

import com.michaelliu.flightfx.domain.model.Currency

/** 計算機顯示狀態:目前基準幣 + 顯示區字串。 */
data class CalculatorUiState(
    val baseCurrency: Currency,
    val display: String,
)
