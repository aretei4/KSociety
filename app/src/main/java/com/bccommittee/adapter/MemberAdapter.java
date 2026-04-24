package com.bccommittee.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bccommittee.R;
import com.bccommittee.model.Member;
import com.bccommittee.util.CurrencyUtil;

import java.util.ArrayList;
import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    public interface OnMemberClickListener {
        void onMemberClick(Member member);
    }

    private final Context context;
    private final OnMemberClickListener listener;
    private List<Member> members = new ArrayList<>();

    public MemberAdapter(Context context, OnMemberClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setMembers(List<Member> members) {
        this.members = new ArrayList<>(members);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        Member m = members.get(position);
        holder.bind(m);
        holder.itemView.setOnClickListener(v -> listener.onMemberClick(m));
    }

    @Override
    public int getItemCount() { return members.size(); }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvName, tvContribution, tvJoinDate, tvLoanAmount, tvLoanLabel;
        FrameLayout frameAvatar;

        MemberViewHolder(View v) {
            super(v);
            tvAvatar       = v.findViewById(R.id.tv_avatar);
            tvName         = v.findViewById(R.id.tv_member_name);
            tvContribution = v.findViewById(R.id.tv_contribution);
            tvJoinDate     = v.findViewById(R.id.tv_join_date);
            tvLoanAmount   = v.findViewById(R.id.tv_loan_amount);
            tvLoanLabel    = v.findViewById(R.id.tv_loan_label);
            frameAvatar    = v.findViewById(R.id.frame_avatar);
        }

        void bind(Member m) {
            tvAvatar.setText(m.getAvatar() != null ? m.getAvatar() : "?");
            tvName.setText(m.getName());
            tvContribution.setText(CurrencyUtil.format(m.getContribution()) + "/mo");
            tvJoinDate.setText(m.getJoinDate() != null ? m.getJoinDate() : "");

            if (m.getAmtBorrowed() > 0) {
                tvLoanAmount.setText(CurrencyUtil.format(m.getAmtBorrowed()));
                tvLoanAmount.setTextColor(
                    itemView.getContext().getColor(R.color.color_orange));
                tvLoanLabel.setText("Loan");
            } else {
                tvLoanAmount.setText("No loan");
                tvLoanAmount.setTextColor(
                    itemView.getContext().getColor(R.color.text_muted));
                tvLoanLabel.setText("");
            }
        }
    }
}
