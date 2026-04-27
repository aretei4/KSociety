package com.khaga.ksociety.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khaga.ksociety.R
import com.khaga.ksociety.databinding.ItemMemberBinding
import com.khaga.ksociety.model.Member
import com.khaga.ksociety.util.CurrencyUtil

class MemberAdapter(
    private val onMemberClick: (Member) -> Unit
) : ListAdapter<Member, MemberAdapter.MemberViewHolder>(MemberDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val binding = ItemMemberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return MemberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class MemberViewHolder(
        private val binding: ItemMemberBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onMemberClick(getItem(adapterPosition))
            }
        }

        fun bind(member: Member) {
            with(binding) {
                tvAvatar.text = member.avatar.ifEmpty { "?" }
                tvMemberName.text = member.name
                tvContribution.text = "${CurrencyUtil.format(member.contribution)}/mo"
                tvJoinDate.text = member.joinDate

                if (member.amtBorrowed > 0) {
                    tvLoanAmount.text = CurrencyUtil.format(member.amtBorrowed)
                    tvLoanAmount.setTextColor(root.context.getColor(R.color.color_orange))
                    tvLoanLabel.text = "Loan"
                } else {
                    tvLoanAmount.text = "No loan"
                    tvLoanAmount.setTextColor(root.context.getColor(R.color.text_muted))
                    tvLoanLabel.text = ""
                }
            }
        }
    }

    private class MemberDiffCallback : DiffUtil.ItemCallback<Member>() {
        override fun areItemsTheSame(old: Member, new: Member) = old.id == new.id
        override fun areContentsTheSame(old: Member, new: Member) = old == new
    }
}
