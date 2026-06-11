package com.michaelliu.flightfx.ui.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
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

    // 只有橫向版面才 include 計算機側欄;以此判斷直向(BottomSheet)/橫向(側欄)兩種呈現
    private val isLandscape get() = binding.calculatorPanel != null

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentCurrencyBinding.inflate(inflater, container, false)

    override fun onViewReady(savedInstanceState: Bundle?) {
        binding.currencyList.adapter = adapter
        binding.currencyList.addItemDecoration(SpacingItemDecoration(requireContext())) // 匯率恆單欄,給均勻列距
        (binding.currencyList.itemAnimator as? SimpleItemAnimator)?.supportsChangeAnimations = false // 切基準不淡出淡入
        defaultListPaddingBottom = binding.currencyList.paddingBottom

        // 橫向側欄接線:隱藏面板自身標題(空間有限),關閉走鍵盤 ▾
        binding.calculatorPanel?.let { CalculatorBinder.bind(it, viewModel, viewLifecycleOwner, showHeader = false) }

        binding.retryButton.setOnClickListener {
            binding.errorView.isVisible = false
            binding.loading.isVisible = true
            viewModel.retry()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.isCalculatorOpen.collect(::renderCalculator) }
            }
        }
    }

    private fun openCalculatorFor(currency: Currency) {
        viewModel.openCalculator()
        if (!isLandscape) {
            reserveListForSheet()
            scrollToCurrency(currency) // 直向:選中卡貼到計算機上緣,清單不被半屏蓋住
        }
        // 橫向:側欄在右側不蓋清單,選中卡靠藍底高亮即可,毋須捲動
    }

    // 依「計算機開關」渲染當前方向的呈現,也負責旋轉/跨方向還原與收掉錯方向的殘留
    private fun renderCalculator(open: Boolean) {
        if (isLandscape) {
            // 橫向:右側恆留空間,未開顯示提示、開啟顯示計算機;清單一律單欄不受影響
            binding.calculatorPanel?.root?.isVisible = open
            binding.calculatorHint?.isVisible = !open
            dismissSheet() // 自直向旋轉過來可能殘留 BottomSheet,橫向不需要
        } else {
            if (open) {
                showSheet()
                reserveListForSheet()
            } else {
                dismissSheet()
                restoreListPadding()
            }
        }
    }

    private fun showSheet() {
        if (childFragmentManager.findFragmentByTag(CalculatorBottomSheetFragment.TAG) == null) {
            CalculatorBottomSheetFragment().show(childFragmentManager, CalculatorBottomSheetFragment.TAG)
        }
    }

    private fun dismissSheet() {
        (childFragmentManager.findFragmentByTag(CalculatorBottomSheetFragment.TAG) as? DialogFragment)
            ?.dismissAllowingStateLoss()
    }

    // 量計算機上緣在螢幕上的 Y,換算出它蓋住清單的高度,設為底部 padding(配 clipToPadding=false)
    private fun reserveListForSheet() {
        val list = binding.currencyList
        list.post {
            val location = IntArray(2)
            list.getLocationOnScreen(location)
            val sheetTopY = (resources.displayMetrics.heightPixels * (1 - CalculatorBottomSheetFragment.SHEET_HEIGHT_RATIO)).toInt()
            list.updatePadding(bottom = (location[1] + list.height - sheetTopY).coerceAtLeast(0))
        }
    }

    // 計算機收起 → 還原清單底部留白並捲回頂
    private fun restoreListPadding() {
        if (view == null) return
        binding.currencyList.updatePadding(bottom = defaultListPaddingBottom)
        binding.currencyList.smoothScrollToPosition(0)
    }

    private fun scrollToCurrency(currency: Currency) {
        val index = adapter.currentList.indexOfFirst { it.currency == currency }
        if (index < 0) return
        val list = binding.currencyList
        list.post {
            val ctx = context ?: return@post
            val scroller = object : LinearSmoothScroller(ctx) {
                override fun getVerticalSnapPreference() = SNAP_TO_END
            }
            scroller.targetPosition = index
            list.layoutManager?.startSmoothScroll(scroller)
        }
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
