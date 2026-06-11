package com.michaelliu.flightfx.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaelliu.flightfx.util.NetworkMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    // 初始值 true:啟動瞬間不閃離線橫幅,等第一筆偵測值落地再說
    val isOnline: StateFlow<Boolean> =
        networkMonitor.isOnline
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), true)

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
