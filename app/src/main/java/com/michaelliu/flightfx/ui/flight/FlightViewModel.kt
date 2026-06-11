package com.michaelliu.flightfx.ui.flight

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaelliu.flightfx.domain.model.Flight
import com.michaelliu.flightfx.domain.model.FlightCategory
import com.michaelliu.flightfx.domain.repository.FlightRepository
import com.michaelliu.flightfx.ui.common.UiState
import com.michaelliu.flightfx.util.AppResult
import com.michaelliu.flightfx.util.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class FlightViewModel @Inject constructor(
    repository: FlightRepository,
) : ViewModel() {

    val uiState: StateFlow<UiState<List<Flight>>> =
        flow {
            while (true) {
                emit(repository.getFlights(FlightCategory.DEFAULT))
                delay(POLL_INTERVAL_MS.milliseconds) // 每 10 秒輪詢
            }
        }
            .map { it.toUiState() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    private companion object {
        const val POLL_INTERVAL_MS = 10_000L
        const val STOP_TIMEOUT_MS = 5_000L // 無人收集後保留上游的緩衝
    }
}

private fun AppResult<List<Flight>>.toUiState(): UiState<List<Flight>> = fold(
    onSuccess = { if (it.isEmpty()) UiState.Empty else UiState.Content(it) },
    onFailure = { UiState.Error(it) },
)
