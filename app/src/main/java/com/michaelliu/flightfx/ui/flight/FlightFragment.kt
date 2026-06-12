package com.michaelliu.flightfx.ui.flight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaelliu.flightfx.R
import com.michaelliu.flightfx.databinding.FragmentFlightBinding
import com.michaelliu.flightfx.domain.model.Flight
import com.michaelliu.flightfx.domain.model.FlightCategory
import com.michaelliu.flightfx.ui.MainViewModel
import com.michaelliu.flightfx.ui.common.BaseFragment
import com.michaelliu.flightfx.ui.common.SpacingItemDecoration
import com.michaelliu.flightfx.ui.common.UiState
import com.michaelliu.flightfx.ui.common.iconRes
import com.michaelliu.flightfx.ui.common.messageRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class FlightFragment : BaseFragment<FragmentFlightBinding>() {

    private val viewModel: FlightViewModel by viewModels()
    private val mainViewModel: MainViewModel by activityViewModels()
    private val adapter = FlightAdapter()
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentFlightBinding.inflate(inflater, container, false)

    override fun onViewReady(savedInstanceState: Bundle?) {
        binding.flightList.adapter = adapter
        // 關掉預設增刪動畫:換類別時清單藏在 shimmer 後面,RecyclerView 不跑 layout,
        // 等轉回可見才補播欠著的「舊卡片淡出」= 閃舊資料;卡片更新提示走自製 pulse,不受影響
        binding.flightList.itemAnimator = null
        binding.flightList.addItemDecoration(SpacingItemDecoration(requireContext())) // 直向單欄 / 橫向 2 欄均分間距
        // 還原篩選選中態要在掛 listener 之前,程式化 check 才不會觸發重抓
        renderCategory(viewModel.category.value)
        binding.lineToggle.addOnButtonCheckedListener { _, _, isChecked ->
            if (isChecked) onFilterChecked()
        }
        binding.ioToggle.addOnButtonCheckedListener { _, _, isChecked ->
            if (isChecked) onFilterChecked()
        }
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        binding.retryButton.setOnClickListener {
            binding.errorView.isVisible = false
            binding.shimmer.isVisible = true
            binding.shimmer.startShimmer()
            viewModel.refresh()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.isRefreshing.collect { binding.swipeRefresh.isRefreshing = it } }
                launch { viewModel.lastUpdated.collect(::renderLastUpdated) }
                // 離線藏重試鈕:連線恢復會自動重抓,離線按了也只會再失敗
                launch { mainViewModel.isOnline.collect { binding.retryButton.isVisible = it } }
            }
        }
    }

    private fun renderCategory(category: FlightCategory) {
        binding.lineToggle.check(
            if (category.isInternational) R.id.filter_international else R.id.filter_domestic
        )
        binding.ioToggle.check(
            if (category.isDeparture) R.id.filter_departure else R.id.filter_arrival
        )
    }

    private fun onFilterChecked() {
        viewModel.selectCategory(
            FlightCategory.of(
                isInternational = binding.lineToggle.checkedButtonId == R.id.filter_international,
                isDeparture = binding.ioToggle.checkedButtonId == R.id.filter_departure,
            )
        )
    }

    private fun render(state: UiState<List<Flight>>) {
        binding.shimmer.isVisible = state is UiState.Loading
        binding.swipeRefresh.isVisible = state is UiState.Content
        binding.emptyView.isVisible = state is UiState.Empty
        binding.errorView.isVisible = state is UiState.Error
        if (state is UiState.Error) {
            binding.errorMessage.setText(state.error.messageRes())
            binding.errorIcon.setImageResource(state.error.iconRes())
        }
        if (state is UiState.Loading) {
            binding.shimmer.startShimmer()
            // 清掉 adapter 殘留的舊類別清單:submitList 的 diff 是非同步的,
            // 不清的話新內容到位前會先閃一下舊資料
            adapter.submitList(null)
        } else {
            binding.shimmer.stopShimmer()
        }
        if (state is UiState.Content) adapter.submitList(state.data)
    }

    private fun renderLastUpdated(millis: Long) {
        if (millis <= 0L) return
        val time = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(timeFormatter)
        binding.lastUpdated.text = getString(R.string.flight_last_updated, time)
        binding.liveDot.isVisible = true // 綠點與時間文字一起現身,不在文字之前先亮
    }

    override fun onDestroyView() {
        binding.flightList.adapter = null
        super.onDestroyView()
    }
}
