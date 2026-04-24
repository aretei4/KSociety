package com.bccommittee.fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bccommittee.R;
import com.bccommittee.adapter.PaymentAdapter;
import com.bccommittee.database.DatabaseHelper;
import com.bccommittee.model.Member;
import com.bccommittee.model.Payment;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PaymentsFragment extends Fragment {

    private long fundId;
    private DatabaseHelper db;
    private PaymentAdapter adapter;
    private LinearLayout layoutEmpty;
    private List<Payment> allPayments = new ArrayList<>();
    private String activeFilter = "all";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_payments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) fundId = getArguments().getLong("fund_id", -1);
        db = DatabaseHelper.getInstance(requireContext());

        RecyclerView rv = view.findViewById(R.id.rv_payments);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PaymentAdapter(requireContext());
        rv.setAdapter(adapter);
        layoutEmpty = view.findViewById(R.id.layout_empty);

        // Filter chips
        setupChips(view);

        // FAB
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_record_payment);
        fab.setOnClickListener(v -> showRecordPaymentDialog());

        loadPayments();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPayments();
    }

    private void setupChips(View view) {
        Chip chipAll     = view.findViewById(R.id.chip_all);
        Chip chipPaid    = view.findViewById(R.id.chip_paid);
        Chip chipPending = view.findViewById(R.id.chip_pending);
        Chip chipInterest= view.findViewById(R.id.chip_interest);

        chipAll.setOnClickListener(v -> { activeFilter = "all";      applyFilter(); });
        chipPaid.setOnClickListener(v -> { activeFilter = "paid";    applyFilter(); });
        chipPending.setOnClickListener(v -> { activeFilter = "pending"; applyFilter(); });
        chipInterest.setOnClickListener(v -> { activeFilter = "interest"; applyFilter(); });
    }

    private void loadPayments() {
        allPayments = db.getPaymentsByFund(fundId);
        applyFilter();
    }

    private void applyFilter() {
        List<Payment> filtered = new ArrayList<>();
        for (Payment p : allPayments) {
            if ("all".equals(activeFilter)) {
                filtered.add(p);
            } else if ("paid".equals(activeFilter) && "paid".equals(p.getStatus())) {
                filtered.add(p);
            } else if ("pending".equals(activeFilter) && "pending".equals(p.getStatus())) {
                filtered.add(p);
            } else if ("interest".equals(activeFilter) &&
                       p.getType() != null && p.getType().contains("Interest")) {
                filtered.add(p);
            }
        }
        adapter.setPayments(filtered);
        layoutEmpty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void showRecordPaymentDialog() {
        List<Member> members = db.getMembersByFund(fundId);
        if (members.isEmpty()) {
            Toast.makeText(requireContext(), "No members yet. Add members first.", Toast.LENGTH_SHORT).show();
            return;
        }

        View dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_record_payment, null);

        // Member spinner
        String[] memberNames = members.stream().map(Member::getName).toArray(String[]::new);

        // Simple dialog for demo; in production use a bottom sheet
        new AlertDialog.Builder(requireContext())
            .setTitle("Record Payment")
            .setView(dialogView)
            .setPositiveButton("Save", (dialog, which) -> {
                // Read values from dialog fields
                EditText etAmount   = dialogView.findViewById(R.id.et_pay_amount);
                EditText etInterest = dialogView.findViewById(R.id.et_pay_interest);
                EditText etPenalty  = dialogView.findViewById(R.id.et_pay_penalty);
                EditText etFee      = dialogView.findViewById(R.id.et_pay_fee);

                long principal = parseLong(etAmount);
                long interest  = parseLong(etInterest);
                long penalty   = parseLong(etPenalty);
                long fee       = parseLong(etFee);
                long total     = principal + interest + penalty + fee;

                if (total == 0) {
                    Toast.makeText(requireContext(), "Enter at least one amount", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Use first member for demo (real app has member selector)
                Member m = members.get(0);
                String date = new SimpleDateFormat("MMM dd", Locale.getDefault()).format(new Date());

                List<String> types = new ArrayList<>();
                if (principal > 0) types.add("Principal");
                if (interest  > 0) types.add("Interest");
                if (penalty   > 0) types.add("Penalty");
                if (fee       > 0) types.add("Fee");

                Payment p = new Payment();
                p.setMemberId(m.getId());
                p.setMemberName(m.getName());
                p.setMemberAvatar(m.getAvatar());
                p.setType(String.join(" + ", types));
                p.setAmount(total);
                p.setPrincipal(principal);
                p.setInterest(interest);
                p.setPenalty(penalty);
                p.setMemberFee(fee);
                p.setDate(date);
                p.setStatus("paid");
                p.setFundId(fundId);

                db.insertPayment(p);
                loadPayments();
                Toast.makeText(requireContext(), "✅ Payment of ₹" + total + " recorded!", Toast.LENGTH_SHORT).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private long parseLong(EditText et) {
        try { return Long.parseLong(et.getText().toString().trim()); }
        catch (Exception e) { return 0; }
    }
}
