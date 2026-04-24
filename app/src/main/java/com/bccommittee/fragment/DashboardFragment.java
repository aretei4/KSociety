package com.bccommittee.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bccommittee.R;
import com.bccommittee.activity.AddMemberActivity;
import com.bccommittee.adapter.PaymentAdapter;
import com.bccommittee.database.DatabaseHelper;
import com.bccommittee.model.Fund;
import com.bccommittee.model.MonthlyReport;
import com.bccommittee.model.Payment;
import com.bccommittee.util.CurrencyUtil;

import java.util.List;

public class DashboardFragment extends Fragment {

    private long fundId;
    private DatabaseHelper db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) fundId = getArguments().getLong("fund_id", -1);
        db = DatabaseHelper.getInstance(requireContext());

        Fund fund = db.getFundById(fundId);
        if (fund == null) return;

        bindStats(view, fund);
        bindRecentPayments(view);
        bindOverdueAlert(view, fund);
        bindQuickActions(view, fund);
    }

    private void bindStats(View v, Fund fund) {
        List<MonthlyReport> reports = db.getReportsByFund(fundId);

        long pool = reports.isEmpty() ? 0 : reports.get(0).getTotalFund();
        long interestMonth = reports.isEmpty() ? 0 : reports.get(0).getInterestIn();

        ((TextView) v.findViewById(R.id.tv_total_pool))
            .setText(CurrencyUtil.format(pool));
        ((TextView) v.findViewById(R.id.tv_this_month))
            .setText(CurrencyUtil.format(fund.getMonthlyAmount()));
        ((TextView) v.findViewById(R.id.tv_interest_month))
            .setText(CurrencyUtil.format(interestMonth));
    }

    private void bindRecentPayments(View v) {
        RecyclerView rv = v.findViewById(R.id.rv_recent_payments);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);

        PaymentAdapter adapter = new PaymentAdapter(requireContext());
        rv.setAdapter(adapter);

        List<Payment> payments = db.getPaymentsByFund(fundId);
        // Show only first 5
        adapter.setPayments(payments.subList(0, Math.min(5, payments.size())));
    }

    private void bindOverdueAlert(View v, Fund fund) {
        LinearLayout alertLayout = v.findViewById(R.id.layout_overdue_alert);
        if (fund.isOverdue() && fund.getOverdueCount() > 0) {
            alertLayout.setVisibility(View.VISIBLE);
            ((TextView) v.findViewById(R.id.tv_overdue_title))
                .setText(fund.getOverdueCount() + " payment(s) overdue");
        } else {
            alertLayout.setVisibility(View.GONE);
        }
    }

    private void bindQuickActions(View v, Fund fund) {
        v.findViewById(R.id.btn_collect_payment).setOnClickListener(btn -> {
            // Navigate to Payments tab (index 2)
            if (getActivity() != null) {
                ((com.google.android.material.tabs.TabLayout)
                    getActivity().findViewById(R.id.tab_layout))
                    .selectTab(((com.google.android.material.tabs.TabLayout)
                        getActivity().findViewById(R.id.tab_layout)).getTabAt(2));
            }
        });

        v.findViewById(R.id.btn_give_loan).setOnClickListener(btn -> {
            if (getActivity() != null) {
                ((com.google.android.material.tabs.TabLayout)
                    getActivity().findViewById(R.id.tab_layout))
                    .selectTab(((com.google.android.material.tabs.TabLayout)
                        getActivity().findViewById(R.id.tab_layout)).getTabAt(1));
            }
        });

        v.findViewById(R.id.btn_add_member).setOnClickListener(btn -> {
            Intent intent = new Intent(requireContext(), AddMemberActivity.class);
            intent.putExtra("fund_id", fundId);
            intent.putExtra("fund_name", fund.getName());
            startActivity(intent);
        });
    }
}
