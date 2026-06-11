package com.michaelliu.flightfx.ui.flight

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

    class FlightViewHolder(private val binding: ItemFlightBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(flight: Flight) = with(binding) {
            val context = root.context
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

        private companion object {
            const val CANCELLED_ALPHA = 0.5f
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<Flight>() {
            override fun areItemsTheSame(old: Flight, new: Flight) = old.flightNo == new.flightNo
            override fun areContentsTheSame(old: Flight, new: Flight) = old == new
        }
    }
}
