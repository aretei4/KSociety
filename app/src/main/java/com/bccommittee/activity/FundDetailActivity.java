package com.bccommittee.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bccommittee.R;
import com.bccommittee.database.DatabaseHelper;
import com.bccommittee.fragment.DashboardFragment;
import com.bccommittee.fragment.MembersFragment;
import com.bccommittee.fragment.PaymentsFragment;
import com.bccommittee.fragment.ReportsFragment;
import com.bccommittee.model.Fund;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class FundDetailActivity extends AppCompatActivity {

    public static final String EXTRA_FUND_ID = "fund_id";

    private Fund fund;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fund_detail);

        db = DatabaseHelper.getInstance(this);
        long fundId = getIntent().getLongExtra(EXTRA_FUND_ID, -1);
        fund = db.getFundById(fundId);
        if (fund == null) { finish(); return; }

        setupHeader();
        setupViewPager();
    }

    private void setupHeader() {
        ((TextView) findViewById(R.id.tv_fund_title)).setText(fund.getName());
        ((TextView) findViewById(R.id.tv_fund_subtitle)).setText("April 2025");
        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(v -> finish());
    }

    private void setupViewPager() {
        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        String[] tabTitles = {"Dashboard", "Members", "Payments", "Reports"};

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public int getItemCount() { return 4; }

            @Override
            public Fragment createFragment(int position) {
                Bundle args = new Bundle();
                args.putLong("fund_id", fund.getId());
                switch (position) {
                    case 0: { DashboardFragment f = new DashboardFragment(); f.setArguments(args); return f; }
                    case 1: { MembersFragment   f = new MembersFragment();   f.setArguments(args); return f; }
                    case 2: { PaymentsFragment  f = new PaymentsFragment();  f.setArguments(args); return f; }
                    case 3: { ReportsFragment   f = new ReportsFragment();   f.setArguments(args); return f; }
                    default: return new DashboardFragment();
                }
            }
        });

        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> tab.setText(tabTitles[position])).attach();
    }

    public Fund getFund() { return fund; }
}
