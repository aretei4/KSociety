package com.khaga.ksociety.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khaga.ksociety.R
import com.khaga.ksociety.databinding.ItemFundBinding
import com.khaga.ksociety.model.Fund
import com.khaga.ksociety.util.CurrencyUtil

class FundAdapter(
    private val onFundClick: (Fund) -> Unit,
    private val onFundLongClick: (Fund) -> Unit = {}
) : ListAdapter<Fund, FundAdapter.FundViewHolder>(FundDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FundViewHolder {
        val binding = ItemFundBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return FundViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FundViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FundViewHolder(
        private val binding: ItemFundBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onFundClick(getItem(adapterPosition))
            }
            binding.root.setOnLongClickListener {
                onFundLongClick(getItem(adapterPosition))
                true
            }
        }

        fun bind(fund: Fund) {
            with(binding) {
                tvFundName.text = fund.name
                tvFundMeta.text = "${fund.totalMembers} members · ${CurrencyUtil.format(fund.monthlyAmount)}/mo"
                tvMonthly.text = CurrencyUtil.format(fund.monthlyAmount)
                tvInterestRate.text = "${fund.interestRate}% / mo"
                tvOverdueCount.text = fund.overdueCount.toString()

                applyDotColor(fund.dotColor)

                when {
                    fund.isClosed -> {
                        tvStatus.text = "Closed"
                        tvStatus.setBackgroundResource(R.drawable.pill_gray_bg)
                        tvStatus.setTextColor(root.context.getColor(R.color.closed_text))
                        tvClosedDate.visibility = View.VISIBLE
                        tvClosedDate.text = "Closed: ${fund.closedDate.orEmpty()}"
                        tvMonthly.text = "—"
                        tvInterestRate.text = "—"
                    }
                    fund.isOverdue -> {
                        tvStatus.text = "⚠ Overdue"
                        tvStatus.setBackgroundResource(R.drawable.pill_orange_bg)
                        tvStatus.setTextColor(root.context.getColor(R.color.color_orange))
                        tvClosedDate.visibility = View.GONE
                    }
                    else -> {
                        tvStatus.text = "✓ All Paid"
                        tvStatus.setBackgroundResource(R.drawable.pill_green_bg)
                        tvStatus.setTextColor(root.context.getColor(R.color.color_green))
                        tvClosedDate.visibility = View.GONE
                    }
                }
            }
        }

        private fun applyDotColor(colorHex: String) {
            runCatching {
                val c = Color.parseColor(colorHex)
                GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(c)
                    binding.viewDot.background = this
                }
                GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 20f
                    setColor(Color.argb(34, Color.red(c), Color.green(c), Color.blue(c)))
                    setStroke(4, Color.argb(85, Color.red(c), Color.green(c), Color.blue(c)))
                    binding.frameDot.background = this
                }
            }
        }
    }

    private class FundDiffCallback : DiffUtil.ItemCallback<Fund>() {
        override fun areItemsTheSame(old: Fund, new: Fund) = old.id == new.id
        override fun areContentsTheSame(old: Fund, new: Fund) = old == new
    }
}
