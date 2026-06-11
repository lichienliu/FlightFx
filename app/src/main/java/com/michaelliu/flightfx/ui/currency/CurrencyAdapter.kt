package com.michaelliu.flightfx.ui.currency

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import com.michaelliu.flightfx.R
import com.michaelliu.flightfx.databinding.ItemCurrencyBinding
import com.michaelliu.flightfx.domain.model.Currency
import com.michaelliu.flightfx.util.CurrencyFormatter

class CurrencyAdapter(
    private val onCurrencyClick: (Currency) -> Unit,
) : ListAdapter<CurrencyRow, CurrencyAdapter.CurrencyViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyViewHolder {
        val binding = ItemCurrencyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CurrencyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CurrencyViewHolder, position: Int) =
        holder.bind(getItem(position))

    inner class CurrencyViewHolder(private val binding: ItemCurrencyBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(row: CurrencyRow) = with(binding) {
            val context = root.context
            code.text = row.currency.name
            name.text = row.currency.chineseName
            amount.text = CurrencyFormatter.formatAmount(row.amount, row.currency)

            baseBadge.isVisible = row.isBase
            rate.isVisible = !row.isBase // 基準卡不顯示比例列(自己對自己恆為 1)
            if (!row.isBase) {
                rate.text = context.getString(
                    R.string.currency_rate,
                    row.baseCurrency.name,
                    CurrencyFormatter.formatRate(row.perBaseRate),
                )
            }

            // 基準卡淺藍高亮,其餘 surface
            val background = if (row.isBase) {
                ContextCompat.getColor(context, R.color.brand_blue_container)
            } else {
                MaterialColors.getColor(root, com.google.android.material.R.attr.colorSurface)
            }
            root.setCardBackgroundColor(background)

            root.setOnClickListener { onCurrencyClick(row.currency) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<CurrencyRow>() {
            override fun areItemsTheSame(old: CurrencyRow, new: CurrencyRow) =
                old.currency == new.currency

            override fun areContentsTheSame(old: CurrencyRow, new: CurrencyRow) = old == new
        }
    }
}
