package com.michaelliu.flightfx.ui.currency

import com.michaelliu.flightfx.domain.model.Currency

/**
 * 匯率清單一列。金額/比例存原始數值,顯示字串交給 Adapter 格式化。
 * 例:基準 USD、輸入 100,JPY 這列 → amount=15420、perBaseRate=154.2、
 * baseCurrency=USD(組出比例列「1 USD = 154.2」)、isBase=false。
 */
data class CurrencyRow(
    /** 這列是哪個幣別 */
    val currency: Currency,
    /** 輸入金額換算成此幣別的結果(卡片右側大字) */
    val amount: Double,
    /** 1 個基準幣兌多少此幣(比例列的數字) */
    val perBaseRate: Double,
    /** 目前的基準幣(比例列「1 ○○○ = …」左邊那個) */
    val baseCurrency: Currency,
    /** 此列就是基準卡:顯示「基準」徽章 + 藍底、隱藏比例列 */
    val isBase: Boolean,
)
