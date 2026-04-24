package com.bccommittee.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bccommittee.R;
import com.bccommittee.adapter.ReportAdapter;
import com.bccommittee.database.DatabaseHelper;
import com.bccommittee.model.MonthlyReport;

import java.util.List;

public class ReportsFragment extends Fragment {

    private long fundId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reports, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) fundId = getArguments().getLong("fund_id", -1);

        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        List<MonthlyReport> reports = db.getReportsByFund(fundId);

        RecyclerView rv = view.findViewById(R.id.rv_reports);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.setNestedScrollingEnabled(false);

        ReportAdapter adapter = new ReportAdapter(requireContext());
        rv.setAdapter(adapter);
        adapter.setReports(reports);
    }
}
