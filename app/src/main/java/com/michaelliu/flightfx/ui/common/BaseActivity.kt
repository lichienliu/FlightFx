package com.michaelliu.flightfx.ui.common

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.viewbinding.ViewBinding

/**
 * 所有 Activity 的基底:集中處理 ViewBinding 生命週期與 edge-to-edge 系統列 inset
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    private var _binding: VB? = null
    protected val binding: VB
        get() = _binding ?: error("binding 僅在 onCreate 之後、onDestroy 之前可用")

    protected abstract fun inflateBinding(): VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = inflateBinding()
        setContentView(binding.root)
        applySystemBarInsets(binding.root)
        onViewReady(savedInstanceState)
    }

    protected open fun onViewReady(savedInstanceState: Bundle?) {}

    /**
     * 將系統列(狀態列 + 導覽列)以 padding 形式加到根 View, 並保留版面原本的 padding
     */
    private fun applySystemBarInsets(root: View) {
        val left = root.paddingLeft
        val top = root.paddingTop
        val right = root.paddingRight
        val bottom = root.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.updatePadding(
                left = left + bars.left,
                top = top + bars.top,
                right = right + bars.right,
                bottom = bottom + bars.bottom,
            )
            insets
        }
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}
