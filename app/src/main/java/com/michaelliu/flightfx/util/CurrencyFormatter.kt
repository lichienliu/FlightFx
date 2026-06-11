package com.michaelliu.flightfx.util

import com.michaelliu.flightfx.domain.model.Currency
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/** 幣別金額/匯率格式化:符號 + 千分位 + 各幣別小數位。 */
object CurrencyFormatter {

    // 最多 2 位小數、千分位、整數去尾零;固定 Locale 避免隨裝置語系改變小數點
    private val numberFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))

    /** 金額含符號,例「¥150」「$1」「7.2 元」 */
    fun formatAmount(amount: Double, currency: Currency): String {
        val number = formatNumber(amount)
        return when (currency) {
            Currency.USD -> "\$$number"
            Currency.JPY -> "¥$number"
            Currency.CNY -> "$number 元"
            Currency.EUR -> "€$number"
            Currency.AUD -> "A\$$number"
            Currency.KRW -> "₩$number"
        }
    }

    /** 純數值(無符號),供比例文字用,例「150」「0.92」 */
    fun formatRate(rate: Double): String = formatNumber(rate)

    private fun formatNumber(value: Double): String = numberFormat.format(value)
}
