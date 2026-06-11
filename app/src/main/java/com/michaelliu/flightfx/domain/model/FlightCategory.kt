package com.michaelliu.flightfx.domain.model

/** 航班類別 */
enum class FlightCategory(val isInternational: Boolean, val isDeparture: Boolean) {
    /** 國際出發 */
    INT_DEP(isInternational = true, isDeparture = true),
    /** 國際抵達 */
    INT_ARR(isInternational = true, isDeparture = false),
    /** 國內出發 */
    DOM_DEP(isInternational = false, isDeparture = true),
    /** 國內抵達 */
    DOM_ARR(isInternational = false, isDeparture = false);

    companion object {
        val DEFAULT = DOM_ARR
    }
}
