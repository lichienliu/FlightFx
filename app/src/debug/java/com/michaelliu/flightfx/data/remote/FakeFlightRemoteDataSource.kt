package com.michaelliu.flightfx.data.remote

import android.content.Context
import com.michaelliu.flightfx.data.remote.dto.FlightDto
import com.michaelliu.flightfx.domain.model.FlightCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject

class FakeFlightRemoteDataSource @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val json: Json,
) : FlightRemoteDataSource {

    private var tick = 0

    override suspend fun getFlights(category: FlightCategory): List<FlightDto> =
        withContext(Dispatchers.IO) {
            delay(600) // 模擬網路延遲,讓載入狀態看得見
            val raw = context.assets
                .open("InstantSchedule_${category.fileCode}.json")
                .bufferedReader()
                .use { it.readText() }
            varyForDemo(json.decodeFromString(raw))
        }

    // 每次輪詢讓其中一班的狀態/實際時間變動,展示 10 秒更新與卡片 pulse
    private fun varyForDemo(flights: List<FlightDto>): List<FlightDto> {
        if (flights.isEmpty()) return flights
        val target = tick % flights.size
        val status = DEMO_STATUSES[tick % DEMO_STATUSES.size]
        val minute = "%02d".format((tick * 7) % 60)
        tick++
        return flights.mapIndexed { i, dto ->
            if (i != target) {
                dto
            } else {
                dto.copy(
                    airFlyStatus = status,
                    realTime = (dto.expectTime ?: "08:00").take(2) + ":" + minute,
                )
            }
        }
    }

    private companion object {
        val DEMO_STATUSES = listOf("準時", "延遲", "登機", "抵達", "延遲登機")
    }
}
