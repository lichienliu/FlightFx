package com.michaelliu.flightfx.data.mapper

import com.michaelliu.flightfx.data.remote.dto.FlightDto
import com.michaelliu.flightfx.domain.model.Flight
import com.michaelliu.flightfx.domain.model.FlightCategory
import com.michaelliu.flightfx.domain.model.FlightStatus

fun FlightDto.toFlight(category: FlightCategory): Flight {
    val scheduled = expectTime.orEmpty() // 預計時間,改點比較基準
    val actual = realTime?.takeIf { it.isNotBlank() } // 實際時間;空字串=尚未發生 → null
    val changed = actual?.takeIf { scheduled.isNotBlank() && it != scheduled } // 實際≠預計 → 改點
    return Flight(
        flightNo = airLineNum.orEmpty(),
        airlineName = airLineName.orEmpty(),
        airlineLogoUrl = airLineLogo?.takeIf { it.isNotBlank() },
        scheduledTime = scheduled,
        actualTime = actual,
        changedTime = changed,
        isDeparture = category.isDeparture,
        airportCode = (if (category.isDeparture) goalAirportCode else upAirportCode).orEmpty(),
        airportName = (if (category.isDeparture) goalAirportName else upAirportName).orEmpty(),
        gate = airBoardingGate?.takeIf { it.isNotBlank() },
        checkInIsland = checkInIsland?.takeIf { it.isNotBlank() },
        checkInCounter = checkInDeskRange?.takeIf { it.isNotBlank() },
        planeType = airPlaneType?.takeIf { it.isNotBlank() },
        status = airFlyStatus.toFlightStatus(),
        delayCause = airFlyDelayCause?.takeIf { it.isNotBlank() },
    )
}

private fun String?.toFlightStatus(): FlightStatus = when (val s = this?.trim().orEmpty()) {
    "離站" -> FlightStatus.Departed
    "抵達" -> FlightStatus.Arrived
    "準時" -> FlightStatus.OnTime
    "取消" -> FlightStatus.Cancelled
    "登機" -> FlightStatus.Boarding
    "延遲" -> FlightStatus.Delayed
    else -> FlightStatus.Unknown(s)
}
