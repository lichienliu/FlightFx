package com.michaelliu.flightfx.ui.currency

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.michaelliu.flightfx.domain.model.Currency
import com.michaelliu.flightfx.domain.model.ExchangeRates
import com.michaelliu.flightfx.domain.repository.CurrencyRepository
import com.michaelliu.flightfx.domain.usecase.CurrencyConverter
import com.michaelliu.flightfx.domain.usecase.ExpressionEvaluator
import com.michaelliu.flightfx.ui.common.UiState
import com.michaelliu.flightfx.util.NetworkMonitor
import com.michaelliu.flightfx.util.fold
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    repository: CurrencyRepository,
    networkMonitor: NetworkMonitor,
) : ViewModel() {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val baseCurrency = MutableStateFlow(Currency.DEFAULT)
    private var latestRates: ExchangeRates? = null

    private val expression = MutableStateFlow("")
    private val amount = MutableStateFlow(DEFAULT_AMOUNT) // 清單換算金額
    // = 或切基準帶入後為 true:下一鍵打數字蓋掉重來、接運算子則沿用目前結果
    private var replaceOnInput = false

    // 計算機是否開啟。撐過旋轉:直向 BottomSheet / 橫向側欄各自據此還原呈現
    private val _isCalculatorOpen = MutableStateFlow(false)
    val isCalculatorOpen: StateFlow<Boolean> = _isCalculatorOpen

    // 匯率抓一次(repository 快取),與基準幣、輸入金額合併 → 切基準/輸入只重算不重打
    val uiState: StateFlow<UiState<List<CurrencyRow>>> =
        combine(
            refreshTrigger.map { repository.getLatestRates() },
            baseCurrency,
            amount,
        ) { result, base, inputAmount ->
            result.fold(
                onSuccess = { rates ->
                    latestRates = rates
                    UiState.Content(buildRows(rates, base, inputAmount))
                },
                onFailure = { UiState.Error(it) },
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UiState.Loading)

    // 計算機顯示狀態(基準幣 + 顯示字串),供 BottomSheet 觀察
    val calculatorState: StateFlow<CalculatorUiState> =
        combine(baseCurrency, expression) { base, expr ->
            CalculatorUiState(base, expr.ifBlank { "0" })
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            CalculatorUiState(Currency.DEFAULT, "0"),
        )

    init {
        // 匯率不輪詢,啟動時離線會停在錯誤頁;連線恢復且仍在錯誤頁時自動重試
        // 剛恢復的瞬間路由/DNS 未必就緒,單發會落空 → 間隔補發,成功或重試用盡即停
        viewModelScope.launch {
            networkMonitor.isOnline.filter { it }.collect {
                repeat(RECONNECT_RETRIES) {
                    if (uiState.value !is UiState.Error) return@collect
                    retry()
                    delay(RECONNECT_RETRY_DELAY_MS)
                }
            }
        }
    }

    fun selectBase(currency: Currency) {
        val previous = baseCurrency.value
        // 先用前一個基準算出帶入金額,再切基準(清單數值連續、僅換基準)
        val converted = latestRates?.let { CurrencyConverter.convert(amount.value, previous, currency, it) }
        baseCurrency.value = currency
        if (converted != null) {
            replaceOnInput = true
            setExpression(formatCarried(converted))
        }
    }

    fun retry() {
        refreshTrigger.tryEmit(Unit)
    }

    fun openCalculator() {
        _isCalculatorOpen.value = true
    }

    fun closeCalculator() {
        _isCalculatorOpen.value = false
    }

    // 數字鍵:單段最多 12 位、全式最多 24 字,超限忽略;段首「0」被新數字取代(避免 07)
    fun onDigit(digit: Char) {
        val current = if (replaceOnInput) "" else expression.value
        replaceOnInput = false
        val lastSegment = current.takeLastWhile { !it.isOperator() }
        if (lastSegment.count { it.isDigit() } >= MAX_SEGMENT_DIGITS || current.length >= MAX_EXPRESSION_LENGTH) return
        setExpression(if (lastSegment == "0") "${current.dropLast(1)}$digit" else "$current$digit")
    }

    // 小數點鍵:每段限一顆;段為空(剛按完運算子)補成「0.」
    fun onDecimal() {
        val current = if (replaceOnInput) "" else expression.value
        replaceOnInput = false
        val lastSegment = current.takeLastWhile { !it.isOperator() }
        if (lastSegment.contains('.') || current.length >= MAX_EXPRESSION_LENGTH) return
        setExpression(if (lastSegment.isEmpty()) "${current}0." else "$current.")
    }

    // 運算子鍵:不允許開頭;連按運算子 = 替換前一顆(「5+」按 × →「5×」)
    fun onOperator(operator: Char) {
        replaceOnInput = false // 不清空:沿用目前結果接著算(8 → 8+)
        val current = expression.value
        if (current.isEmpty()) return
        setExpression(if (current.last().isOperator()) "${current.dropLast(1)}$operator" else "$current$operator")
    }

    // C 鍵:全清,清單金額同步歸 0
    fun onClear() {
        replaceOnInput = false
        setExpression("")
    }

    // 退格鍵:刪一個字元,刪光時清單金額歸 0
    fun onBackspace() {
        replaceOnInput = false
        setExpression(expression.value.dropLast(1))
    }

    // = 鍵:求值成功才把結果寫回顯示框;失敗(如 ÷0)畫面不動
    fun onEquals() {
        val result = ExpressionEvaluator.evaluate(expression.value) ?: return
        replaceOnInput = true
        setExpression(RESULT_FORMAT.format(result))
    }

    // 顯示字串與換算金額同步;空字串視為 0、非法保留前值;清單金額夾為非負(換匯不顯示負值)
    private fun setExpression(value: String) {
        expression.value = value
        val evaluated = if (value.isEmpty()) 0.0 else ExpressionEvaluator.evaluate(value) ?: amount.value
        amount.value = evaluated.coerceAtLeast(0.0)
    }

    // 切換基準帶入的金額:保留 ~6 位有效數字並去尾零(小額不會被四捨五入成 0)
    private fun formatCarried(value: Double): String =
        BigDecimal.valueOf(value).round(MathContext(6, RoundingMode.HALF_UP)).stripTrailingZeros().toPlainString()

    private fun buildRows(rates: ExchangeRates, base: Currency, inputAmount: Double): List<CurrencyRow> =
        Currency.entries.map { target ->
            CurrencyRow(
                currency = target,
                amount = CurrencyConverter.convert(inputAmount, base, target, rates) ?: 0.0,
                perBaseRate = CurrencyConverter.convert(1.0, base, target, rates) ?: 0.0,
                baseCurrency = base,
                isBase = target == base,
            )
        }

    private fun Char.isOperator() = this == '+' || this == '−' || this == '×' || this == '÷'

    private companion object {
        const val DEFAULT_AMOUNT = 1.0
        const val STOP_TIMEOUT_MS = 5_000L
        const val RECONNECT_RETRIES = 3
        const val RECONNECT_RETRY_DELAY_MS = 5_000L
        const val MAX_SEGMENT_DIGITS = 12
        const val MAX_EXPRESSION_LENGTH = 24
        val RESULT_FORMAT = DecimalFormat("0.##########", DecimalFormatSymbols(Locale.US))
    }
}
