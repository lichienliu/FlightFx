package com.michaelliu.flightfx.domain.usecase

import com.michaelliu.flightfx.domain.model.Currency
import com.michaelliu.flightfx.domain.model.ExchangeRates

/**
 * USD 交叉匯率換算。缺率回 null。
 * freecurrencyapi 免費方案 base 固定 USD,rates 全是「1 USD 兌該幣」的比率;
 * 任意 from→to:先除 rate[from] 折回 USD,再乘 rate[to] 換到目標幣,
 * 即 金額_to = 金額_from × (rate[to] / rate[from])。
 * 例:rate[JPY]=150、rate[KRW]=1350,300 JPY → 300 × (1350/150) = 2700 KRW。
 */
object CurrencyConverter {
    fun convert(amount: Double, from: Currency, to: Currency, rates: ExchangeRates): Double? {
        val rateFrom = rates.rates[from] ?: return null
        val rateTo = rates.rates[to] ?: return null
        return amount * (rateTo / rateFrom)
    }
}
