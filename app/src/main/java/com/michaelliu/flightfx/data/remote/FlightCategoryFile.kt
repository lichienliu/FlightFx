package com.michaelliu.flightfx.data.remote

import com.michaelliu.flightfx.domain.model.FlightCategory

// 代號填進 InstantSchedule_{category}.json
internal val FlightCategory.fileCode: String
    get() = when (this) {
        FlightCategory.INT_DEP -> "INTDEP"
        FlightCategory.INT_ARR -> "INTARR"
        FlightCategory.DOM_DEP -> "DOMDEP"
        FlightCategory.DOM_ARR -> "DOMARR"
    }
