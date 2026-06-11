package com.michaelliu.flightfx.ui.currency

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.michaelliu.flightfx.R
import com.michaelliu.flightfx.databinding.FragmentCalculatorBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CalculatorBottomSheetFragment : BottomSheetDialogFragment() {

    // 與 CurrencyFragment 共用同一個 ViewModel → 鍵盤輸入直接驅動上方清單
    private val viewModel: CurrencyViewModel by viewModels({ requireParentFragment() })

    private var _binding: FragmentCalculatorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCalculatorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wireKeypad()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.calculatorState.collect { state ->
                    binding.currencyLabel.text = getString(
                        R.string.calculator_currency_label,
                        state.baseCurrency.name,
                        state.baseCurrency.chineseName,
                    )
                    binding.display.text = state.display
                }
            }
        }
    }

    // 半屏(約 58%)+ 調淡遮罩,讓上方清單看得見即時連動
    override fun onStart() {
        super.onStart()
        val sheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        sheet.layoutParams = sheet.layoutParams.apply {
            height = (resources.displayMetrics.heightPixels * SHEET_HEIGHT_RATIO).toInt()
        }
        BottomSheetBehavior.from(sheet).apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed = true
            isDraggable = false // 不可滑動收起,只能點外部或 ▾
        }
        dialog?.window?.setDimAmount(DIM_AMOUNT)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        (parentFragment as? CurrencyFragment)?.onCalculatorDismissed()
    }

    private fun wireKeypad() = with(binding) {
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
        keyCollapse.setOnClickListener { dismiss() }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    companion object {
        const val TAG = "calculator"
        const val SHEET_HEIGHT_RATIO = 0.58f
        private const val DIM_AMOUNT = 0.2f
    }
}
