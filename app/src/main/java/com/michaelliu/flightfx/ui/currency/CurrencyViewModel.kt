package com.michaelliu.flightfx.ui.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaelliu.flightfx.domain.model.Currency
import com.michaelliu.flightfx.domain.model.ExchangeRates
import com.michaelliu.flightfx.domain.repository.CurrencyRepository
import com.michaelliu.flightfx.domain.usecase.CurrencyConverter
import com.michaelliu.flightfx.ui.common.UiState
import com.michaelliu.flightfx.util.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    repository: CurrencyRepository,
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val baseCurrency = MutableStateFlow(Currency.DEFAULT)

    // 匯率抓一次(repository 快取),與基準幣合併 → 切基準不重打 API,只重算
    val uiState: StateFlow<UiState<List<CurrencyRow>>> =
        combine(
            refreshTrigger.map { repository.getLatestRates() },
            baseCurrency,
        ) { result, base ->
            result.fold(
                onSuccess = { UiState.Content(buildRows(it, base)) },
                onFailure = { UiState.Error(it) },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    fun selectBase(currency: Currency) {
        baseCurrency.value = currency
    }

    fun retry() {
        refreshTrigger.tryEmit(Unit)
    }

    private fun buildRows(rates: ExchangeRates, base: Currency): List<CurrencyRow> =
        Currency.entries.map { target ->
            CurrencyRow(
                currency = target,
                amount = CurrencyConverter.convert(DEFAULT_AMOUNT, base, target, rates) ?: 0.0,
                perBaseRate = CurrencyConverter.convert(1.0, base, target, rates) ?: 0.0,
                baseCurrency = base,
                isBase = target == base,
            )
        }

    private companion object {
        const val DEFAULT_AMOUNT = 1.0
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
