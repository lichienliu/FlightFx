package com.michaelliu.flightfx.ui.flight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaelliu.flightfx.databinding.FragmentFlightBinding
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    adapter.submitList((state as? UiState.Content)?.data.orEmpty())
                }
            }
        }
    }
}
