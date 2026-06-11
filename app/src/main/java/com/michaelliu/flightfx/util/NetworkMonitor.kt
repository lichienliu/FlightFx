package com.michaelliu.flightfx.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 連線狀態偵測:把 ConnectivityManager 的 callback 包成 Flow。
 * 判準用 VALIDATED(系統實測通得了外網),不是只看有網卡;
 * 追蹤預設網路,Wi-Fi ↔ 行動數據切換不會被多網路事件混淆。
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: Flow<Boolean> = callbackFlow {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                trySend(capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            }

            override fun onLost(network: Network) {
                // 換網路時舊網路先 lost,重查現況而非斷言離線;但斷線瞬間 activeNetwork
                // 可能仍回報拆除中的這張網卡,要排除它,否則誤判在線後再無事件修正
                val active = connectivityManager.activeNetwork
                trySend(active != null && active != network && isValidated(active))
            }
        }
        connectivityManager.registerDefaultNetworkCallback(callback)
        trySend(isCurrentlyOnline()) // 完全沒網路時 callback 不會發初始事件,補一發現況
        awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
    }.distinctUntilChanged()

    private fun isCurrentlyOnline(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        return isValidated(network)
    }

    private fun isValidated(network: Network): Boolean =
        connectivityManager.getNetworkCapabilities(network)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
}
