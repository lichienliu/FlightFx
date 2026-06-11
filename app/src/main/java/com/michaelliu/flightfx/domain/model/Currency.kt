package com.michaelliu.flightfx.domain.model

/** 匯率支援的六種幣別 */
enum class Currency(val chineseName: String) {
    JPY("日圓"),
    USD("美元"),
    CNY("人民幣"),
    EUR("歐元"),
    AUD("澳幣"),
    KRW("韓元");

    companion object {
        val DEFAULT = USD
    }
}
