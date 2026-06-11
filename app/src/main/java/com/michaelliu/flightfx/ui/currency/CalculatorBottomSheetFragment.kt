package com.michaelliu.flightfx.ui.currency

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.michaelliu.flightfx.databinding.FragmentCalculatorBinding
import dagger.hilt.android.AndroidEntryPoint

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
        CalculatorBinder.bind(binding, viewModel, viewLifecycleOwner) // 鍵盤/顯示接線與橫向側欄共用
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

    // 點外部/返回取消 → 同步關閉旗標;實際收起與清單還原由 CurrencyFragment 依旗標處理
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        viewModel.closeCalculator()
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
