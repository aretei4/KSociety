package com.bccommittee.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bccommittee.R;
import com.bccommittee.adapter.FundAdapter;
import com.bccommittee.database.DatabaseHelper;
import com.bccommittee.model.Fund;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FundAdapter.OnFundClickListener {

    private TextView tvGreeting, tvTotalFunds, tvTotalMembers, tvOverdue;
    private FundAdapter fundAdapter;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = DatabaseHelper.getInstance(this);
        initViews();
        setupFundsList();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData();
    }

    private void initViews() {
        tvGreeting     = findViewById(R.id.tv_greeting);
        tvTotalFunds   = findViewById(R.id.tv_total_funds);
        tvTotalMembers = findViewById(R.id.tv_total_members);
        tvOverdue      = findViewById(R.id.tv_overdue);

        // Backup button
        FrameLayout btnBackup = findViewById(R.id.btn_backup);
        btnBackup.setOnClickListener(v ->
                startActivity(new Intent(this, BackupActivity.class)));

        setGreeting();
    }

    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12)      greeting = "Good Morning \uD83D\uDC4B";
        else if (hour < 17) greeting = "Good Afternoon \uD83D\uDC4B";
        else                greeting = "Good Evening \uD83D\uDC4B";
        tvGreeting.setText(greeting);
    }

    private void setupFundsList() {
        FrameLayout contentFrame = findViewById(R.id.content_frame);

        // Build RecyclerView programmatically and add into the FrameLayout
        RecyclerView rv = new RecyclerView(this);
        FrameLayout.LayoutParams rvParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        rv.setLayoutParams(rvParams);
        int p = dp(16);
        rv.setPadding(p, p, p, dp(88));
        rv.setClipToPadding(false);
        rv.setLayoutManager(new LinearLayoutManager(this));

        fundAdapter = new FundAdapter(this, this);
        rv.setAdapter(fundAdapter);
        contentFrame.addView(rv);

        // FAB
        ExtendedFloatingActionButton fab = new ExtendedFloatingActionButton(this);
        FrameLayout.LayoutParams fabParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        fabParams.gravity = android.view.Gravity.BOTTOM | android.view.Gravity.END;
        fabParams.setMargins(0, 0, dp(20), dp(20));
        fab.setLayoutParams(fabParams);
        fab.setText("+ New Fund");
        fab.setTextColor(getResources().getColor(R.color.white, getTheme()));
        fab.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                getResources().getColor(R.color.header_green, getTheme())));
        fab.setOnClickListener(v ->
                startActivity(new Intent(this, AddFundActivity.class)));
        contentFrame.addView(fab);
    }

    private void refreshData() {
        List<Fund> funds = db.getAllFunds();
        fundAdapter.setFunds(funds);

        long active = 0;
        for (Fund f : funds) if (!f.isClosed()) active++;
        tvTotalFunds.setText(String.valueOf(active));
        tvTotalMembers.setText(String.valueOf(db.getTotalMembersCount()));
        tvOverdue.setText(String.valueOf(db.getOverdueFundsCount()));
    }

    @Override
    public void onFundClick(Fund fund) {
        Intent intent = new Intent(this, FundDetailActivity.class);
        intent.putExtra(FundDetailActivity.EXTRA_FUND_ID, fund.getId());
        startActivity(intent);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
