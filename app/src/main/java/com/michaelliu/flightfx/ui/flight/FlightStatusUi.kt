package com.michaelliu.flightfx.ui.flight

import android.content.Context
import androidx.annotation.ColorRes
import com.michaelliu.flightfx.R
import com.michaelliu.flightfx.domain.model.FlightStatus

data class FlightStatusUi(@get:ColorRes val colorRes: Int, val label: String)

fun FlightStatus.toUi(context: Context): FlightStatusUi = when (this) {
    FlightStatus.Departed -> FlightStatusUi(R.color.status_positive, context.getString(R.string.flight_status_departed))
    FlightStatus.Arrived -> FlightStatusUi(R.color.status_positive, context.getString(R.string.flight_status_arrived))
    FlightStatus.OnTime -> FlightStatusUi(R.color.brand_blue, context.getString(R.string.flight_status_ontime))
    FlightStatus.Boarding -> FlightStatusUi(R.color.brand_blue, context.getString(R.string.flight_status_boarding))
    FlightStatus.Delayed -> FlightStatusUi(R.color.status_warning, context.getString(R.string.flight_status_delayed))
    FlightStatus.Cancelled -> FlightStatusUi(R.color.status_negative, context.getString(R.string.flight_status_cancelled))
    is FlightStatus.Unknown -> FlightStatusUi(R.color.status_neutral, raw)
}
