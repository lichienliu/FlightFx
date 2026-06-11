package com.michaelliu.flightfx.ui.flight

import android.view.LayoutInflater
import android.view.ViewGroup
import com.michaelliu.flightfx.databinding.FragmentFlightBinding
import com.michaelliu.flightfx.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FlightFragment : BaseFragment<FragmentFlightBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentFlightBinding.inflate(inflater, container, false)
}
