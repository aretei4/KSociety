package com.bccommittee.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bccommittee.R;
import com.bccommittee.model.Payment;
import com.bccommittee.util.CurrencyUtil;

import java.util.ArrayList;
import java.util.List;

public class PaymentAdapter extends RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder> {

    private final Context context;
    private List<Payment> payments = new ArrayList<>();

    public PaymentAdapter(Context context) {
        this.context = context;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = new ArrayList<>(payments);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_payment, parent, false);
        return new PaymentViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        holder.bind(payments.get(position));
    }

    @Override
    public int getItemCount() { return payments.size(); }

    class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvMemberName, tvTypeDate, tvAmount, tvStatus;

        PaymentViewHolder(View v) {
            super(v);
            tvAvatar     = v.findViewById(R.id.tv_avatar);
            tvMemberName = v.findViewById(R.id.tv_member_name);
            tvTypeDate   = v.findViewById(R.id.tv_type_date);
            tvAmount     = v.findViewById(R.id.tv_amount);
            tvStatus     = v.findViewById(R.id.tv_status);
        }

        void bind(Payment p) {
            tvAvatar.setText(p.getMemberAvatar() != null ? p.getMemberAvatar() : "?");
            tvMemberName.setText(p.getMemberName());
            tvTypeDate.setText(p.getType() + " · " + (p.getDate() != null ? p.getDate() : ""));

            if (p.getAmount() > 0) {
                tvAmount.setText(CurrencyUtil.format(p.getAmount()));
            } else {
                tvAmount.setText("—");
            }

            switch (p.getStatus() != null ? p.getStatus() : "") {
                case "paid":
                    tvStatus.setText("Paid");
                    tvStatus.setBackgroundResource(R.drawable.pill_green_bg);
                    tvStatus.setTextColor(context.getColor(R.color.color_green));
                    tvAmount.setTextColor(context.getColor(R.color.color_green));
                    tvAvatar.setTextColor(context.getColor(R.color.color_green));
                    break;
                case "pending":
                    tvStatus.setText("Pending");
                    tvStatus.setBackgroundResource(R.drawable.pill_orange_bg);
                    tvStatus.setTextColor(context.getColor(R.color.color_orange));
                    tvAmount.setTextColor(context.getColor(R.color.color_orange));
                    tvAvatar.setTextColor(context.getColor(R.color.color_orange));
                    break;
                default:
                    tvStatus.setText("No Loan");
                    tvStatus.setBackgroundResource(R.drawable.pill_gray_bg);
                    tvStatus.setTextColor(context.getColor(R.color.closed_text));
                    tvAmount.setTextColor(context.getColor(R.color.text_muted));
                    tvAvatar.setTextColor(context.getColor(R.color.text_muted));
                    break;
            }
        }
    }
}
