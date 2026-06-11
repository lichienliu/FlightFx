package com.michaelliu.flightfx.ui.flight

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.michaelliu.flightfx.R
import com.michaelliu.flightfx.databinding.FragmentFlightBinding
import com.michaelliu.flightfx.domain.model.Flight
import com.michaelliu.flightfx.ui.common.BaseFragment
import com.michaelliu.flightfx.ui.common.UiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FlightFragment : BaseFragment<FragmentFlightBinding>() {

    private val viewModel: FlightViewModel by viewModels()

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentFlightBinding.inflate(inflater, container, false)

    override fun onViewReady(savedInstanceState: Bundle?) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    private fun render(state: UiState<List<Flight>>) {
        binding.placeholder.text = when (state) {
            UiState.Loading -> getString(R.string.state_loading)
            UiState.Empty -> getString(R.string.state_empty)
            is UiState.Content -> getString(R.string.flight_count, state.data.size)
            is UiState.Error -> getString(R.string.state_error, state.error.toString())
        }
    }
}
