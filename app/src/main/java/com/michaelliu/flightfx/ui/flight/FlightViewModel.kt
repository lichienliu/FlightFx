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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class FlightViewModel @Inject constructor(
    repository: FlightRepository,
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastUpdated = MutableStateFlow(0L)
    val lastUpdated: StateFlow<Long> = _lastUpdated.asStateFlow()

    val uiState: StateFlow<UiState<List<Flight>>> =
        merge(
            tickerFlow(),
            refreshTrigger.onEach { _isRefreshing.value = true },
        )
            .map {
                val result = repository.getFlights(FlightCategory.DEFAULT)
                _isRefreshing.value = false // 抓取結束就收圈,與資料有沒有變無關
                if (result is AppResult.Success) _lastUpdated.value = System.currentTimeMillis()
                result.toUiState()
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    fun refresh() {
        refreshTrigger.tryEmit(Unit)
    }

    private fun tickerFlow() = flow {
        while (true) {
            emit(Unit)
            delay(POLL_INTERVAL_MS.milliseconds) // 每 10 秒輪詢
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 10_000L
        const val STOP_TIMEOUT_MS = 5_000L // 無人收集 5 秒後停止上游
    }
}

private fun AppResult<List<Flight>>.toUiState(): UiState<List<Flight>> = fold(
    onSuccess = { if (it.isEmpty()) UiState.Empty else UiState.Content(it) },
    onFailure = { UiState.Error(it) },
)
