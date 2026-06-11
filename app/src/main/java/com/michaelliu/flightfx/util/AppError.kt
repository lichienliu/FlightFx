package com.michaelliu.flightfx.util

sealed interface AppError {
    val cause: Throwable?

    /** 無網路 / 連線逾時 / IO 中斷。 */
    data class Network(override val cause: Throwable? = null) : AppError

    /** 伺服器回非 2xx,帶 [code]。 */
    data class Server(val code: Int, override val cause: Throwable? = null) : AppError

    /** 回應無法解析(JSON 格式非預期)。 */
    data class Parse(override val cause: Throwable? = null) : AppError

    /** 其他未歸類的錯誤。 */
    data class Unknown(override val cause: Throwable? = null) : AppError
}
