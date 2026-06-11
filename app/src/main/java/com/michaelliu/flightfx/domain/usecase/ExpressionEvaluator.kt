package com.michaelliu.flightfx.domain.usecase

import com.notkamui.keval.Keval
import com.notkamui.keval.KevalException

/**
 * 四則運算求值:委派 Keval(純 Kotlin;× ÷ 優先、左結合、支援正負號)。
 * wrapper 統一邊界:鍵盤顯示符(× ÷ −)轉 ASCII、打到一半的尾端運算子 / 小數點忽略、
 * 例外收斂成 null(非法 / ÷0)、非有限結果(溢位)回 null。
 */
object ExpressionEvaluator {

    fun evaluate(expression: String): Double? {
        val normalized = expression
            .replace('×', '*').replace('÷', '/').replace('−', '-')
            .replace(DANGLING_DOT, "") // 打到一半的「5.」寬容為「5」
            .trimEnd { it in OPERATORS } // 打到一半的「5+」寬容為「5」
        if (normalized.isEmpty()) return null
        return try {
            Keval.eval(normalized)
        } catch (_: KevalException) {
            null // 每個按鍵都會求值,「算不出來」是常態而非錯誤
        }?.takeIf { it.isFinite() }
    }

    private const val OPERATORS = "+-*/"
    private val DANGLING_DOT = Regex("""\.(?!\d)""") // 後面不接數字的小數點
}
