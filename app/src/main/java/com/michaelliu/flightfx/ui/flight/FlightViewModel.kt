package com.michaelliu.flightfx.ui.flight

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaelliu.flightfx.domain.model.Flight
import com.michaelliu.flightfx.domain.model.FlightCategory
import com.michaelliu.flightfx.domain.repository.FlightRepository
import com.michaelliu.flightfx.ui.common.UiState
import com.michaelliu.flightfx.util.AppResult
import com.michaelliu.flightfx.util.NetworkMonitor
import com.michaelliu.flightfx.util.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class FlightViewModel @Inject constructor(
    private val repository: FlightRepository,
    networkMonitor: NetworkMonitor,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastUpdated = MutableStateFlow(0L)
    val lastUpdated: StateFlow<Long> = _lastUpdated.asStateFlow()

    // 目前顯示的航班類別;放 SavedStateHandle,程序被殺重生也回到原類別
    val category: StateFlow<FlightCategory> =
        savedStateHandle.getStateFlow(KEY_CATEGORY, FlightCategory.DEFAULT)

    // 最後一次成功結果;之後的抓取失敗就沿用它(清單不清空),首抓就失敗才進錯誤頁
    private var lastSuccess: UiState<List<Flight>>? = null

    // 已呈現過的類別;用來區分「換類別」與「同類別重新訂閱」(回分頁不閃 shimmer)
    private var shownCategory = category.value

    // 外層接類別、內層接網路:換類別整段重啟回 Loading;網路切換只重啟內層,
    // 清單照 stale-while-error 留在畫面。離線收掉 ticker 停止輪詢、只留手動刷新;
    // 恢復時 flatMapLatest 重啟 ticker → 立即自動重抓
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState<List<Flight>>> =
        category
            .flatMapLatest { cat ->
                networkMonitor.isOnline
                    .flatMapLatest { online ->
                        _isRefreshing.value = false // 切換會中斷抓取中的刷新,先收圈避免卡轉圈
                        if (online) {
                            merge(tickerFlow(), manualRefresh())
                        } else {
                            // 離線也先抓一次:快速失敗進錯誤頁(或沿用前值),不卡在載入畫面
                            manualRefresh().onStart { emit(Unit) }
                        }
                    }
                    .map { fetch(cat) }
                    .onStart {
                        if (cat != shownCategory) {
                            // 換類別:棄前值再回 Loading,舊類別清單才不會冒充新類別
                            shownCategory = cat
                            lastSuccess = null
                            emit(UiState.Loading)
                        }
                    }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    fun selectCategory(category: FlightCategory) {
        savedStateHandle[KEY_CATEGORY] = category // StateFlow 同值去重,重選同一格不會重抓
    }

    fun refresh() {
        refreshTrigger.tryEmit(Unit)
    }

    private suspend fun fetch(category: FlightCategory): UiState<List<Flight>> {
        val result = repository.getFlights(category)
        // 取消發生在 suspension point:換類別瞬間,舊類別的請求可能剛好完成、
        // 還來得及跑到這裡;比對 shownCategory 擋下它的副作用,別污染新類別的前值
        if (category != shownCategory) return UiState.Loading
        _isRefreshing.value = false // 抓取結束就收圈,與資料有沒有變無關
        if (result is AppResult.Success) _lastUpdated.value = System.currentTimeMillis()
        return result.fold(
            onSuccess = { flights ->
                (if (flights.isEmpty()) UiState.Empty else UiState.Content(flights))
                    .also { lastSuccess = it }
            },
            onFailure = { lastSuccess ?: UiState.Error(it) },
        )
    }

    private fun manualRefresh() = refreshTrigger.onEach { _isRefreshing.value = true }

    private fun tickerFlow() = flow {
        while (true) {
            emit(Unit)
            delay(POLL_INTERVAL_MS.milliseconds) // 每 10 秒輪詢
        }
    }

    private companion object {
        const val POLL_INTERVAL_MS = 10_000L
        const val STOP_TIMEOUT_MS = 5_000L // 無人收集 5 秒後停止上游
        const val KEY_CATEGORY = "flight_category"
    }
}
