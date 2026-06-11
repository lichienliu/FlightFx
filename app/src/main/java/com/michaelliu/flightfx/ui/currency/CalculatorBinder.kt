package com.michaelliu.flightfx.ui.currency

import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaelliu.flightfx.R
import com.michaelliu.flightfx.databinding.FragmentCalculatorBinding
import kotlinx.coroutines.launch

/**
 * 計算機面板(fragment_calculator)的共用接線:鍵盤事件 → ViewModel、ViewModel 狀態 → 顯示區。
 *
 * 同一份面板在直向是 BottomSheet 內容、橫向是側欄,邏輯完全相同 → 抽出共用,避免兩處重複。
 * 關閉一律走 [CurrencyViewModel.closeCalculator](單一事實來源),實際收起由 CurrencyFragment 依方向處理。
 */
object CalculatorBinder {

    fun bind(
        panel: FragmentCalculatorBinding,
        viewModel: CurrencyViewModel,
        owner: LifecycleOwner,
        showHeader: Boolean = true, // 橫向側欄空間有限,隱藏幣別名稱 + 副標(基準幣靠左清單徽章即可辨識)
    ) {
        panel.calcHeader.isVisible = showHeader
        wireKeypad(panel, viewModel)
        owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.calculatorState.collect { state ->
                    panel.currencyLabel.text = panel.root.context.getString(
                        R.string.calculator_currency_label,
                        state.baseCurrency.name,
                        state.baseCurrency.chineseName,
                    )
                    panel.display.text = state.display
                }
            }
        }
    }

    private fun wireKeypad(panel: FragmentCalculatorBinding, viewModel: CurrencyViewModel) = with(panel) {
        listOf(
            key0 to '0', key1 to '1', key2 to '2', key3 to '3', key4 to '4',
            key5 to '5', key6 to '6', key7 to '7', key8 to '8', key9 to '9',
        ).forEach { (button, digit) -> button.setOnClickListener { viewModel.onDigit(digit) } }
        listOf(keyPlus to '+', keyMinus to '−', keyMultiply to '×', keyDivide to '÷')
            .forEach { (button, operator) -> button.setOnClickListener { viewModel.onOperator(operator) } }
        keyDot.setOnClickListener { viewModel.onDecimal() }
        keyClear.setOnClickListener { viewModel.onClear() }
        keyBackspace.setOnClickListener { viewModel.onBackspace() }
        keyEquals.setOnClickListener { viewModel.onEquals() }
        keyCollapse.setOnClickListener { viewModel.closeCalculator() }
    }
}
