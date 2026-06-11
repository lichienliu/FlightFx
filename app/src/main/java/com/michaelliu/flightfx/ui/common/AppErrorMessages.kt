package com.michaelliu.flightfx.ui.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.michaelliu.flightfx.R
import com.michaelliu.flightfx.util.AppError

@StringRes
fun AppError.messageRes(): Int = when (this) {
    is AppError.Network -> R.string.error_network
    is AppError.Server -> R.string.error_server
    is AppError.Parse -> R.string.error_parse
    is AppError.Unknown -> R.string.error_generic
}

@DrawableRes
fun AppError.iconRes(): Int = when (this) {
    is AppError.Network -> R.drawable.ic_wifi_off_24
    else -> R.drawable.ic_error_outline_24
}
