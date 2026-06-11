package com.michaelliu.flightfx.domain.model

/** 航班狀態。 */
sealed interface FlightStatus {
    /** 離站 */
    data object Departed : FlightStatus
    /** 抵達 */
    data object Arrived : FlightStatus
    /** 準時 */
    data object OnTime : FlightStatus
    /** 取消 */
    data object Cancelled : FlightStatus
    /** 登機 */
    data object Boarding : FlightStatus
    /** 延遲 */
    data object Delayed : FlightStatus
    /** 未知 */
    data class Unknown(val raw: String) : FlightStatus
}
