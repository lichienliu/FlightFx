package com.michaelliu.flightfx.ui.currency

import android.view.LayoutInflater
import android.view.ViewGroup
import com.michaelliu.flightfx.databinding.FragmentCurrencyBinding
import com.michaelliu.flightfx.ui.common.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrencyFragment : BaseFragment<FragmentCurrencyBinding>() {

    override fun inflateBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentCurrencyBinding.inflate(inflater, container, false)
}
