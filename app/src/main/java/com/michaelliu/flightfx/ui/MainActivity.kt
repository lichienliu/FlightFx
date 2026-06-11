package com.michaelliu.flightfx.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.fragment.app.commit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigationrail.NavigationRailView
import com.michaelliu.flightfx.R
import com.michaelliu.flightfx.databinding.ActivityMainBinding
import com.michaelliu.flightfx.ui.common.BaseActivity
import com.michaelliu.flightfx.ui.currency.CurrencyFragment
import com.michaelliu.flightfx.ui.flight.FlightFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val viewModel: MainViewModel by viewModels()

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
        // 兩 variant 同 ID 不同型別 → ViewBinding 退型成 View,監聽器處 cast 回共同父型 NavigationBarView
        (binding.navBar as NavigationBarView).setOnItemSelectedListener { item ->
            showFragment(if (item.itemId == R.id.nav_currency) TAG_CURRENCY else TAG_FLIGHT)
            true
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // 離線橫幅兩分頁共用;content_area 開 animateLayoutChanges,顯隱時內容平滑推移
                viewModel.isOnline.collect { binding.offlineBanner.isVisible = !it }
            }
        }
    }

    // 覆寫 BaseActivity 預設 inset:依方向把系統列分給導覽列與內容,避免雙重留白
    // 直向 navBar 在底、橫向 navBar 是左側 rail,吃的邊不同,用型別分流
    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            // 含 displayCutout:橫向時前鏡頭挖孔在側邊,內容/rail 不可壓在其下
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            if (binding.navBar is NavigationRailView) {
                // 橫向:rail 吃 左+上+下,內容吃 上+右+下
                binding.navBar.updatePadding(left = bars.left, top = bars.top, bottom = bars.bottom)
                binding.contentArea.updatePadding(top = bars.top, right = bars.right, bottom = bars.bottom)
            } else {
                // 直向:內容吃上、BottomNav 吃下
                binding.contentArea.updatePadding(top = bars.top)
                binding.navBar.updatePadding(bottom = bars.bottom)
            }
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
