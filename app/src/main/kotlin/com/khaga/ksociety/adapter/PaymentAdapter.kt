package com.khaga.ksociety.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khaga.ksociety.R
import com.khaga.ksociety.databinding.ItemPaymentBinding
import com.khaga.ksociety.model.Payment
import com.khaga.ksociety.util.CurrencyUtil

class PaymentAdapter : ListAdapter<Payment, PaymentAdapter.PaymentViewHolder>(PaymentDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
        val binding = ItemPaymentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return PaymentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class PaymentViewHolder(
        private val binding: ItemPaymentBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(payment: Payment) {
            with(binding) {
                tvAvatar.text = payment.memberAvatar.ifEmpty { "?" }
                tvMemberName.text = payment.memberName
                tvTypeDate.text = "${payment.type} · ${payment.date}"
                tvAmount.text = if (payment.amount > 0) CurrencyUtil.format(payment.amount) else "—"

                when (payment.status) {
                    "paid" -> {
                        tvStatus.text = "Paid"
                        tvStatus.setBackgroundResource(R.drawable.pill_green_bg)
                        tvStatus.setTextColor(root.context.getColor(R.color.color_green))
                        tvAmount.setTextColor(root.context.getColor(R.color.color_green))
                        tvAvatar.setTextColor(root.context.getColor(R.color.color_green))
                    }
                    "pending" -> {
                        tvStatus.text = "Pending"
                        tvStatus.setBackgroundResource(R.drawable.pill_orange_bg)
                        tvStatus.setTextColor(root.context.getColor(R.color.color_orange))
                        tvAmount.setTextColor(root.context.getColor(R.color.color_orange))
                        tvAvatar.setTextColor(root.context.getColor(R.color.color_orange))
                    }
                    else -> {
                        tvStatus.text = "—"
                        tvStatus.setBackgroundResource(R.drawable.pill_gray_bg)
                        tvStatus.setTextColor(root.context.getColor(R.color.closed_text))
                        tvAmount.setTextColor(root.context.getColor(R.color.text_muted))
                        tvAvatar.setTextColor(root.context.getColor(R.color.text_muted))
                    }
                }
            }
        }
    }

    private class PaymentDiffCallback : DiffUtil.ItemCallback<Payment>() {
        override fun areItemsTheSame(old: Payment, new: Payment) = old.id == new.id
        override fun areContentsTheSame(old: Payment, new: Payment) = old == new
    }
}
