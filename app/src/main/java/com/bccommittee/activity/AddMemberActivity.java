package com.bccommittee.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bccommittee.R;
import com.bccommittee.database.DatabaseHelper;
import com.bccommittee.model.Fund;
import com.bccommittee.model.Member;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddMemberActivity extends AppCompatActivity {

    private EditText etName, etPhone, etContribution, etFee, etLoan;
    private Button btnSave;
    private LinearLayout layoutPreview;
    private FrameLayout frameAvatarPreview;
    private TextView tvAvatarPreview, tvPreviewName, tvPreviewDetail, tvFundName;
    private DatabaseHelper db;
    private long fundId;
    private Fund fund;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member);

        db = DatabaseHelper.getInstance(this);
        fundId = getIntent().getLongExtra("fund_id", -1);
        fund   = db.getFundById(fundId);

        initViews();
        setupListeners();
        prefillFromFund();
    }

    private void initViews() {
        etName          = findViewById(R.id.et_name);
        etPhone         = findViewById(R.id.et_phone);
        etContribution  = findViewById(R.id.et_contribution);
        etFee           = findViewById(R.id.et_fee);
        etLoan          = findViewById(R.id.et_loan);
        btnSave         = findViewById(R.id.btn_save_member);
        layoutPreview   = findViewById(R.id.layout_preview);
        frameAvatarPreview = findViewById(R.id.frame_avatar_preview);
        tvAvatarPreview = findViewById(R.id.tv_avatar_preview);
        tvPreviewName   = findViewById(R.id.tv_preview_name);
        tvPreviewDetail = findViewById(R.id.tv_preview_detail);
        tvFundName      = findViewById(R.id.tv_fund_name);

        ((ImageButton) findViewById(R.id.btn_close)).setOnClickListener(v -> finish());

        if (fund != null) {
            tvFundName.setText("to " + fund.getName());
        }
    }

    private void prefillFromFund() {
        if (fund != null) {
            if (fund.getMonthlyAmount() > 0)
                etContribution.setText(String.valueOf(fund.getMonthlyAmount()));
            if (fund.getMemberFee() > 0)
                etFee.setText(String.valueOf(fund.getMemberFee()));
        }
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
        etContribution.addTextChangedListener(watcher);
        btnSave.setOnClickListener(v -> saveMember());
    }

    private void updatePreview() {
        String name = etName.getText().toString().trim();
        if (!name.isEmpty()) {
            String initials = Member.generateAvatar(name);
            frameAvatarPreview.setVisibility(View.VISIBLE);
            tvAvatarPreview.setText(initials);
            layoutPreview.setVisibility(View.VISIBLE);
            tvPreviewName.setText(name);

            long contrib = parseLong(etContribution);
            long fee     = parseLong(etFee);
            long loan    = parseLong(etLoan);
            StringBuilder detail = new StringBuilder("₹" + contrib + "/mo");
            if (fee > 0)  detail.append(" · Fee ₹").append(fee);
            if (loan > 0) detail.append(" · Loan ₹").append(loan);
            tvPreviewDetail.setText(detail.toString());

            // Also update avatar in preview card
            TextView pvAvatar = findViewById(R.id.tv_preview_avatar);
            if (pvAvatar != null) pvAvatar.setText(initials);
        } else {
            frameAvatarPreview.setVisibility(View.GONE);
            layoutPreview.setVisibility(View.GONE);
        }
    }

    private void validateForm() {
        boolean valid = !etName.getText().toString().trim().isEmpty()
                     && !etContribution.getText().toString().trim().isEmpty();
        btnSave.setEnabled(valid);
        btnSave.setAlpha(valid ? 1.0f : 0.5f);
    }

    private void saveMember() {
        String name    = etName.getText().toString().trim();
        String phone   = etPhone.getText().toString().trim();
        long contrib   = parseLong(etContribution);
        long fee       = parseLong(etFee);
        long loan      = parseLong(etLoan);
        String date    = new SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(new Date());

        Member member = new Member();
        member.setName(name);
        member.setPhone(phone);
        member.setAvatar(Member.generateAvatar(name));
        member.setContribution(contrib);
        member.setFees(fee);
        member.setAmtBorrowed(loan);
        member.setJoinDate(date);
        member.setFundId(fundId);

        long id = db.insertMember(member);
        if (id > 0) {
            Toast.makeText(this, "✅ " + name + " added!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Failed to add member. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private long parseLong(EditText et) {
        try { return Long.parseLong(et.getText().toString().trim()); }
        catch (Exception e) { return 0; }
    }
}
