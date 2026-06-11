package com.michaelliu.flightfx.ui.flight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaelliu.flightfx.databinding.FragmentFlightBinding
import com.michaelliu.flightfx.domain.model.Flight
import com.michaelliu.flightfx.ui.common.BaseFragment
import com.michaelliu.flightfx.ui.common.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FlightFragment : BaseFragment<FragmentFlightBinding>() {

    private val viewModel: FlightViewModel by viewModels()
    private val adapter = FlightAdapter()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentFlightBinding.inflate(inflater, container, false)

    override fun onViewReady(savedInstanceState: Bundle?) {
        binding.flightList.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        binding.retryButton.setOnClickListener { viewModel.refresh() }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect(::render) }
                launch { viewModel.isRefreshing.collect { binding.swipeRefresh.isRefreshing = it } }
            }
        }
    }

    private fun render(state: UiState<List<Flight>>) {
        binding.shimmer.isVisible = state is UiState.Loading
        binding.swipeRefresh.isVisible = state is UiState.Content
        binding.emptyView.isVisible = state is UiState.Empty
        binding.errorView.isVisible = state is UiState.Error
        if (state is UiState.Loading) binding.shimmer.startShimmer() else binding.shimmer.stopShimmer()
        if (state is UiState.Content) adapter.submitList(state.data)
    }
}
