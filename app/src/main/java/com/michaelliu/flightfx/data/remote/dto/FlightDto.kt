package com.michaelliu.flightfx.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class FlightDto(
    val expectTime: String? = null,
    val realTime: String? = null,
    val airLineName: String? = null,
    val airLineCode: String? = null,
    val airLineLogo: String? = null,
    val airLineUrl: String? = null,
    val airLineNum: String? = null,
    val goalAirportCode: String? = null,
    val goalAirportName: String? = null,
    val upAirportCode: String? = null,
    val upAirportName: String? = null,
    val checkInIsland: String? = null,
    val checkInDeskRange: String? = null,
    val airPlaneType: String? = null,
    val airBoardingGate: String? = null,
    val airFlyStatus: String? = null,
    val airFlyDelayCause: String? = null,
)
