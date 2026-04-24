package com.bccommittee.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bccommittee.R;
import com.bccommittee.database.DatabaseHelper;
import com.bccommittee.model.Fund;

public class AddFundActivity extends AppCompatActivity {

    private EditText etName, etMonthly, etNumMembers, etInterestRate, etMemberFee;
    private Button btnCreate;
    private LinearLayout layoutPreview;
    private TextView tvPreviewName, tvPreviewMeta;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fund);

        db = DatabaseHelper.getInstance(this);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etName         = findViewById(R.id.et_fund_name);
        etMonthly      = findViewById(R.id.et_monthly_amount);
        etNumMembers   = findViewById(R.id.et_num_members);
        etInterestRate = findViewById(R.id.et_interest_rate);
        etMemberFee    = findViewById(R.id.et_member_fee);
        btnCreate      = findViewById(R.id.btn_create_fund);
        layoutPreview  = findViewById(R.id.layout_preview);
        tvPreviewName  = findViewById(R.id.tv_preview_name);
        tvPreviewMeta  = findViewById(R.id.tv_preview_meta);

        ((ImageButton) findViewById(R.id.btn_close)).setOnClickListener(v -> finish());
    }

    private void setupListeners() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePreview();
                validateForm();
            }
        };

        etName.addTextChangedListener(watcher);
        etMonthly.addTextChangedListener(watcher);
        etNumMembers.addTextChangedListener(watcher);

        btnCreate.setOnClickListener(v -> saveFund());
    }

    private void updatePreview() {
        String name = etName.getText().toString().trim();
        if (!name.isEmpty()) {
            layoutPreview.setVisibility(View.VISIBLE);
            tvPreviewName.setText(name);
            int members = parseIntSafe(etNumMembers);
            long monthly = parseLongSafe(etMonthly);
            tvPreviewMeta.setText(members + " members · ₹" + monthly + "/mo");
        } else {
            layoutPreview.setVisibility(View.GONE);
        }
    }

    private void validateForm() {
        boolean valid = !etName.getText().toString().trim().isEmpty()
                     && !etMonthly.getText().toString().trim().isEmpty()
                     && !etNumMembers.getText().toString().trim().isEmpty();
        btnCreate.setEnabled(valid);
        btnCreate.setAlpha(valid ? 1.0f : 0.5f);
    }

    private void saveFund() {
        String name = etName.getText().toString().trim();
        long monthly     = parseLongSafe(etMonthly);
        int numMembers   = parseIntSafe(etNumMembers);
        float intRate    = parseFloatSafe(etInterestRate, 2.0f);
        long memberFee   = parseLongSafe(etMemberFee);

        Fund fund = new Fund();
        fund.setName(name);
        fund.setMonthlyAmount(monthly);
        fund.setTotalMembers(numMembers);
        fund.setInterestRate(intRate);
        fund.setMemberFee(memberFee);
        fund.setStatus("allpaid");
        fund.setOverdueCount(0);
        fund.setDotColor("#1F5C3A");

        long id = db.insertFund(fund);
        if (id > 0) {
            Toast.makeText(this, "🎉 Fund '" + name + "' created!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to create fund. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private int parseIntSafe(EditText et) {
        try { return Integer.parseInt(et.getText().toString().trim()); }
        catch (Exception e) { return 0; }
    }

    private long parseLongSafe(EditText et) {
        try { return Long.parseLong(et.getText().toString().trim()); }
        catch (Exception e) { return 0; }
    }

    private float parseFloatSafe(EditText et, float def) {
        try { return Float.parseFloat(et.getText().toString().trim()); }
        catch (Exception e) { return def; }
    }
}
