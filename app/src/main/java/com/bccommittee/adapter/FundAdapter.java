package com.bccommittee.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bccommittee.R;
import com.bccommittee.model.Fund;
import com.bccommittee.util.CurrencyUtil;

import java.util.ArrayList;
import java.util.List;

public class FundAdapter extends RecyclerView.Adapter<FundAdapter.FundViewHolder> {

    public interface OnFundClickListener {
        void onFundClick(Fund fund);
    }

    private final Context context;
    private final OnFundClickListener listener;
    private List<Fund> funds = new ArrayList<>();

    public FundAdapter(Context context, OnFundClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setFunds(List<Fund> funds) {
        this.funds = funds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FundViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_fund, parent, false);
        return new FundViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FundViewHolder holder, int position) {
        Fund fund = funds.get(position);
        holder.bind(fund);
        holder.itemView.setOnClickListener(v -> listener.onFundClick(fund));
    }

    @Override
    public int getItemCount() {
        return funds.size();
    }

    static class FundViewHolder extends RecyclerView.ViewHolder {
        TextView tvFundName, tvFundMeta, tvStatus, tvMonthly,
                 tvInterestRate, tvOverdueCount, tvClosedDate;
        View viewDot;
        FrameLayout frameDot;

        FundViewHolder(View v) {
            super(v);
            tvFundName     = v.findViewById(R.id.tv_fund_name);
            tvFundMeta     = v.findViewById(R.id.tv_fund_meta);
            tvStatus       = v.findViewById(R.id.tv_status);
            tvMonthly      = v.findViewById(R.id.tv_monthly);
            tvInterestRate = v.findViewById(R.id.tv_interest_rate);
            tvOverdueCount = v.findViewById(R.id.tv_overdue_count);
            tvClosedDate   = v.findViewById(R.id.tv_closed_date);
            viewDot        = v.findViewById(R.id.view_dot);
            frameDot       = v.findViewById(R.id.frame_dot);
        }

        void bind(Fund fund) {
            tvFundName.setText(fund.getName());
            tvFundMeta.setText(fund.getTotalMembers() + " members · "
                    + CurrencyUtil.format(fund.getMonthlyAmount()) + "/mo");
            tvMonthly.setText(CurrencyUtil.format(fund.getMonthlyAmount()));
            tvInterestRate.setText(fund.getInterestRate() + "% / mo");
            tvOverdueCount.setText(String.valueOf(fund.getOverdueCount()));

            // Dot color
            try {
                int dotColor = Color.parseColor(
                        fund.getDotColor() != null ? fund.getDotColor() : "#1F5C3A");
                GradientDrawable dotBg = new GradientDrawable();
                dotBg.setShape(GradientDrawable.OVAL);
                dotBg.setColor(dotColor);
                viewDot.setBackground(dotBg);

                GradientDrawable frameBg = new GradientDrawable();
                frameBg.setShape(GradientDrawable.RECTANGLE);
                frameBg.setCornerRadius(20f);
                frameBg.setColor(Color.argb(34, Color.red(dotColor),
                        Color.green(dotColor), Color.blue(dotColor)));
                frameBg.setStroke(4, Color.argb(85, Color.red(dotColor),
                        Color.green(dotColor), Color.blue(dotColor)));
                frameDot.setBackground(frameBg);
            } catch (Exception ignored) {}

            // Status pill
            if (fund.isClosed()) {
                tvStatus.setText("Closed");
                tvStatus.setBackgroundResource(R.drawable.pill_gray_bg);
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.closed_text));
                tvClosedDate.setVisibility(View.VISIBLE);
                tvClosedDate.setText("Closed: " + (fund.getClosedDate() != null ? fund.getClosedDate() : ""));
                tvMonthly.setText("—");
                tvInterestRate.setText("—");
            } else if (fund.isOverdue()) {
                tvStatus.setText("⚠ Overdue");
                tvStatus.setBackgroundResource(R.drawable.pill_orange_bg);
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.color_orange));
                tvClosedDate.setVisibility(View.GONE);
            } else {
                tvStatus.setText("✓ All Paid");
                tvStatus.setBackgroundResource(R.drawable.pill_green_bg);
                tvStatus.setTextColor(itemView.getContext().getColor(R.color.color_green));
                tvClosedDate.setVisibility(View.GONE);
            }
        }
    }
}
