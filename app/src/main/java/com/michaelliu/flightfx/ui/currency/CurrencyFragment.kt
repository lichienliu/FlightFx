package com.michaelliu.flightfx.ui.currency

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaelliu.flightfx.databinding.FragmentCurrencyBinding
import com.michaelliu.flightfx.ui.common.BaseFragment
import com.michaelliu.flightfx.ui.common.UiState
import com.michaelliu.flightfx.ui.common.messageRes
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CurrencyFragment : BaseFragment<FragmentCurrencyBinding>() {

    private val viewModel: CurrencyViewModel by viewModels()
    private val adapter = CurrencyAdapter { viewModel.selectBase(it) }

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentCurrencyBinding.inflate(inflater, container, false)

    override fun onViewReady(savedInstanceState: Bundle?) {
        binding.currencyList.adapter = adapter
        binding.retryButton.setOnClickListener {
            binding.errorView.isVisible = false
            binding.loading.isVisible = true
            viewModel.retry()
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
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
