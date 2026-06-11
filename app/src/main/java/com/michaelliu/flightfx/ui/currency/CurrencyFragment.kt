package com.michaelliu.flightfx.ui.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.SimpleItemAnimator
import com.michaelliu.flightfx.databinding.FragmentCurrencyBinding
import com.michaelliu.flightfx.domain.model.Currency
import com.michaelliu.flightfx.ui.common.BaseFragment
import com.michaelliu.flightfx.ui.common.SpacingItemDecoration
import com.michaelliu.flightfx.ui.common.UiState
import com.michaelliu.flightfx.ui.common.messageRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CurrencyFragment : BaseFragment<FragmentCurrencyBinding>() {

    private val viewModel: CurrencyViewModel by viewModels()
    private val adapter = CurrencyAdapter { currency ->
        viewModel.selectBase(currency)
        openCalculatorFor(currency)
    }

    private var defaultListPaddingBottom = 0

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentCurrencyBinding.inflate(inflater, container, false)

    override fun onViewReady(savedInstanceState: Bundle?) {
        binding.currencyList.adapter = adapter
        binding.currencyList.addItemDecoration(SpacingItemDecoration(requireContext())) // 直向單欄 / 橫向 2 欄均分間距
        (binding.currencyList.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false // 切基準不淡出淡入
        defaultListPaddingBottom = binding.currencyList.paddingBottom
        // 重建後計算機若仍在(旋轉/主題切換),重套清單底部預留,避免下半被蓋住
        if (childFragmentManager.findFragmentByTag(CalculatorBottomSheetFragment.TAG) != null) {
            reserveListForSheet()
        }
        binding.retryButton.setOnClickListener {
            binding.errorView.isVisible = false
            binding.loading.isVisible = true
            viewModel.retry()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    // 計算機半屏會蓋住下半清單:預留被蓋住的高度,並把選中幣別平滑貼齊到計算機上緣(其餘排在其上),不留可見空白
    private fun openCalculatorFor(currency: Currency) {
        if (childFragmentManager.findFragmentByTag(CalculatorBottomSheetFragment.TAG) == null) {
            CalculatorBottomSheetFragment().show(childFragmentManager, CalculatorBottomSheetFragment.TAG)
        }
        reserveListForSheet()
        val list = binding.currencyList
        val index = adapter.currentList.indexOfFirst { it.currency == currency }
        if (index < 0) return
        list.post {
            val ctx = context ?: return@post
            val scroller = object : LinearSmoothScroller(ctx) {
                override fun getVerticalSnapPreference() = SNAP_TO_END
            }
            scroller.targetPosition = index
            (list.layoutManager as LinearLayoutManager).startSmoothScroll(scroller)
        }
    }

    // 量計算機上緣在螢幕上的 Y,換算出它蓋住清單的高度,設為底部 padding
    private fun reserveListForSheet() {
        val list = binding.currencyList
        list.post {
            val location = IntArray(2)
            list.getLocationOnScreen(location)
            val sheetTopY = (resources.displayMetrics.heightPixels * (1 - CalculatorBottomSheetFragment.SHEET_HEIGHT_RATIO)).toInt()
            list.updatePadding(bottom = (location[1] + list.height - sheetTopY).coerceAtLeast(0))
        }
    }

    // 計算機收起 → 還原清單底部留白
    fun onCalculatorDismissed() {
        if (view == null) return
        binding.currencyList.updatePadding(bottom = defaultListPaddingBottom)
        binding.currencyList.smoothScrollToPosition(0)
    }

    private fun render(state: UiState<List<CurrencyRow>>) {
        binding.loading.isVisible = state is UiState.Loading
        binding.currencyList.isVisible = state is UiState.Content
        binding.emptyView.isVisible = state is UiState.Empty
        binding.errorView.isVisible = state is UiState.Error
        if (state is UiState.Error) binding.errorMessage.setText(state.error.messageRes())
        if (state is UiState.Content) adapter.submitList(state.data)
    }

    override fun onDestroyView() {
        binding.currencyList.adapter = null
        super.onDestroyView()
    }
}
