package com.michaelliu.flightfx.ui

import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import com.michaelliu.flightfx.R
import com.michaelliu.flightfx.databinding.ActivityMainBinding
import com.michaelliu.flightfx.ui.common.BaseActivity
import com.michaelliu.flightfx.ui.currency.CurrencyFragment
import com.michaelliu.flightfx.ui.flight.FlightFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateBinding() = ActivityMainBinding.inflate(layoutInflater)

    override fun onViewReady(savedInstanceState: Bundle?) {
        applyWindowInsets()
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                val currency = CurrencyFragment()
                add(R.id.nav_host_container, FlightFragment(), TAG_FLIGHT)
                add(R.id.nav_host_container, currency, TAG_CURRENCY)
                hide(currency)
                setMaxLifecycle(currency, Lifecycle.State.STARTED)
            }
        }
        binding.bottomNav.setOnItemSelectedListener { item ->
            showFragment(if (item.itemId == R.id.nav_currency) TAG_CURRENCY else TAG_FLIGHT)
            true
        }
    }

    // 覆寫 BaseActivity 預設 inset:狀態列 padding 給內容、導覽列 padding 給 BottomNav(底部不留白)
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.navHostContainer.updatePadding(top = bars.top)
            binding.bottomNav.updatePadding(bottom = bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(binding.root)
    }

    private fun showFragment(tag: String) {
        val target = supportFragmentManager.findFragmentByTag(tag) ?: return
        supportFragmentManager.commit {
            supportFragmentManager.fragments.forEach {
                if (it === target) {
                    show(it)
                    setMaxLifecycle(it, Lifecycle.State.RESUMED)
                } else {
                    hide(it)
                    setMaxLifecycle(it, Lifecycle.State.STARTED)
                }
            }
        }
    }

    private companion object {
        const val TAG_FLIGHT = "flight"
        const val TAG_CURRENCY = "currency"
    }
}
