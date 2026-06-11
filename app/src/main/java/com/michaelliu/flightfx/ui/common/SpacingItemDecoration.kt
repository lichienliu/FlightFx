package com.michaelliu.flightfx.ui.common

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 清單間距裝飾。水平方向依 spanCount 均分溝槽,使「外緣」與「中縫」都等於同一寬度
 * (1 欄與 2 欄外觀一致、2 欄中縫不會變兩倍);垂直方向給固定列距。
 *
 * 取代各 item 卡片自帶的 margin —— margin 在 2 欄時相鄰兩格會疊成雙倍中縫,改由此處統一分配。
 */
class SpacingItemDecoration(
    context: Context,
    horizontalDp: Int = 16,
    verticalDp: Int = 12,
) : RecyclerView.ItemDecoration() {

    private val h = dp(context, horizontalDp)
    private val v = dp(context, verticalDp)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val spanCount = (parent.layoutManager as? GridLayoutManager)?.spanCount ?: 1
        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return
        val column = position % spanCount

        // 均分公式:左右兩側加總恆為 h → 每格內容等寬,且外緣與中縫皆等於 h
        outRect.left = h - column * h / spanCount
        outRect.right = (column + 1) * h / spanCount
        if (position < spanCount) outRect.top = v // 只有第一列補上緣
        outRect.bottom = v
    }

    private fun dp(context: Context, value: Int): Int =
        (value * context.resources.displayMetrics.density).toInt()
}
