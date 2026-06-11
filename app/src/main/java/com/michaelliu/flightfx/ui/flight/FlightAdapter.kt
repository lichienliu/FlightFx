package com.michaelliu.flightfx.ui.flight

import android.animation.ValueAnimator
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil3.load
import coil3.request.crossfade
import com.google.android.material.color.MaterialColors
import com.michaelliu.flightfx.R
import com.michaelliu.flightfx.databinding.ItemFlightBinding
import com.michaelliu.flightfx.domain.model.Flight

class FlightAdapter : ListAdapter<Flight, FlightAdapter.FlightViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlightViewHolder {
        val binding = ItemFlightBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FlightViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlightViewHolder, position: Int) =
        holder.bind(getItem(position))

    // 內容變更會走 payload 路徑(getChangePayload 非 null):跳過預設淡入淡出,改 rebind + pulse 提示「這筆剛更新」
    override fun onBindViewHolder(holder: FlightViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            holder.bind(getItem(position))
            holder.pulse()
        }
    }

    class FlightViewHolder(private val binding: ItemFlightBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var pulseAnimator: ValueAnimator? = null

        fun bind(flight: Flight) = with(binding) {
            val context = root.context
            // 回收復用時先取消殘留的 pulse 動畫、底色歸位
            pulseAnimator?.cancel()
            root.setCardBackgroundColor(surfaceColor())

            logo.load(flight.airlineLogoUrl) { crossfade(true) }
            airlineName.text = flight.airlineName
            flightNo.text = flight.flightNo
            scheduledTime.text = flight.scheduledTime

            changedTime.isVisible = flight.changedTime != null
            flight.changedTime?.let {
                changedTime.text = context.getString(R.string.flight_changed_time, it)
            }

            val routeRes =
                if (flight.isDeparture) R.string.flight_route_departure else R.string.flight_route_arrival
            route.text = context.getString(routeRes, flight.airportName)

            gate.isVisible = flight.gate != null
            flight.gate?.let { gate.text = context.getString(R.string.flight_gate, it) }

            planeType.isVisible = flight.planeType != null
            flight.planeType?.let { planeType.text = context.getString(R.string.flight_plane, it) }

            val status = flight.status.toUi(context)
            statusText.text = status.label
            statusText.setTextColor(ContextCompat.getColor(context, status.colorRes))
            statusDot.backgroundTintList =
                ContextCompat.getColorStateList(context, status.colorRes)

            // 取消班次:整卡半透明 + 表定時間加刪除線(or = 加上刪除線、and inv() = 移除刪除線)
            // 移除不能省:ViewHolder 會復用,上一筆若是取消班次,殘留的刪除線要清掉
            root.alpha = if (flight.isCancelled) CANCELLED_ALPHA else 1f
            scheduledTime.paintFlags = if (flight.isCancelled) {
                scheduledTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                scheduledTime.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        // 剛更新的卡片背景閃一下品牌色再淡回 surface
        fun pulse() {
            val highlight = ContextCompat.getColor(binding.root.context, R.color.brand_blue_container)
            pulseAnimator?.cancel()
            pulseAnimator = ValueAnimator.ofArgb(highlight, surfaceColor()).apply {
                duration = PULSE_MS
                addUpdateListener { binding.root.setCardBackgroundColor(it.animatedValue as Int) }
                start()
            }
        }

        private fun surfaceColor() =
            MaterialColors.getColor(binding.root, com.google.android.material.R.attr.colorSurface)

        private companion object {
            const val CANCELLED_ALPHA = 0.5f
            const val PULSE_MS = 700L
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Flight>() {
            // 同班號一天可能多班:班號+表定時間 複合鍵才算同一筆
            override fun areItemsTheSame(old: Flight, new: Flight) =
                old.flightNo == new.flightNo && old.scheduledTime == new.scheduledTime
            override fun areContentsTheSame(old: Flight, new: Flight) = old == new
            override fun getChangePayload(old: Flight, new: Flight): Any = Unit // 非 null → 走上面 payload 綁定
        }
    }
}
