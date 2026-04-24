package com.bccommittee.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bccommittee.R;
import com.bccommittee.api.RetrofitClient;
import com.bccommittee.database.DatabaseHelper;
import com.bccommittee.util.BackupManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupActivity extends AppCompatActivity {

    private static final String PREFS_BACKUP = "bc_backup_prefs";
    private static final String KEY_LAST_BACKUP = "last_backup_time";

    private EditText etApiUrl;
    private Button btnBackup, btnRestore, btnSaveUrl;
    private ProgressBar progressBar;
    private LinearLayout layoutResult;
    private TextView tvResultIcon, tvResultMessage, tvLastBackup;
    private TextView tvStatFunds, tvStatMembers, tvStatPayments;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        db = DatabaseHelper.getInstance(this);
        initViews();
        loadCurrentUrl();
        refreshStats();
        showLastBackupTime();
    }

    private void initViews() {
        ((ImageButton) findViewById(R.id.btn_back)).setOnClickListener(v -> finish());

        etApiUrl       = findViewById(R.id.et_api_url);
        btnBackup      = findViewById(R.id.btn_backup);
        btnRestore     = findViewById(R.id.btn_restore);
        btnSaveUrl     = findViewById(R.id.btn_save_url);
        progressBar    = findViewById(R.id.progress_bar);
        layoutResult   = findViewById(R.id.layout_result);
        tvResultIcon   = findViewById(R.id.tv_result_icon);
        tvResultMessage= findViewById(R.id.tv_result_message);
        tvLastBackup   = findViewById(R.id.tv_last_backup);
        tvStatFunds    = findViewById(R.id.tv_stat_funds);
        tvStatMembers  = findViewById(R.id.tv_stat_members);
        tvStatPayments = findViewById(R.id.tv_stat_payments);

        btnSaveUrl.setOnClickListener(v -> saveUrl());
        btnBackup.setOnClickListener(v -> performBackup());
        btnRestore.setOnClickListener(v -> performRestore());
    }

    private void loadCurrentUrl() {
        String url = RetrofitClient.getInstance(this).getCurrentBaseUrl();
        etApiUrl.setText(url);
    }

    private void saveUrl() {
        String url = etApiUrl.getText().toString().trim();
        if (url.isEmpty()) {
            Toast.makeText(this, "Please enter a valid URL", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!url.startsWith("http")) {
            Toast.makeText(this, "URL must start with http:// or https://", Toast.LENGTH_SHORT).show();
            return;
        }
        RetrofitClient.setBaseUrl(this, url);
        Toast.makeText(this, "✓ API endpoint saved", Toast.LENGTH_SHORT).show();
    }

    private void refreshStats() {
        int funds    = db.getAllFundsForBackup().size();
        int members  = db.getAllMembersForBackup().size();
        int payments = db.getAllPaymentsForBackup().size();

        tvStatFunds.setText(String.valueOf(funds));
        tvStatMembers.setText(String.valueOf(members));
        tvStatPayments.setText(String.valueOf(payments));
    }

    private void performBackup() {
        setLoading(true);
        layoutResult.setVisibility(View.GONE);

        BackupManager.performBackup(this, new BackupManager.BackupCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showResult(true, message);
                    saveLastBackupTime();
                    showLastBackupTime();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showResult(false, error);
                });
            }
        });
    }

    private void performRestore() {
        setLoading(true);
        layoutResult.setVisibility(View.GONE);

        BackupManager.performRestore(this, new BackupManager.BackupCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showResult(true, message);
                    refreshStats();
                });
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> {
                    setLoading(false);
                    showResult(false, error);
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnBackup.setEnabled(!loading);
        btnRestore.setEnabled(!loading);
        btnBackup.setAlpha(loading ? 0.5f : 1.0f);
        btnRestore.setAlpha(loading ? 0.5f : 1.0f);
    }

    private void showResult(boolean success, String message) {
        layoutResult.setVisibility(View.VISIBLE);
        tvResultIcon.setText(success ? "✅" : "❌");
        tvResultMessage.setText(message);
        tvResultMessage.setTextColor(getColor(success ? R.color.color_green : R.color.color_red));
    }

    private void saveLastBackupTime() {
        long now = System.currentTimeMillis();
        getSharedPreferences(PREFS_BACKUP, MODE_PRIVATE)
            .edit().putLong(KEY_LAST_BACKUP, now).apply();
    }

    private void showLastBackupTime() {
        long last = getSharedPreferences(PREFS_BACKUP, MODE_PRIVATE)
            .getLong(KEY_LAST_BACKUP, 0);
        if (last > 0) {
            String time = new SimpleDateFormat("dd MMM yyyy, hh:mm a",
                Locale.getDefault()).format(new Date(last));
            tvLastBackup.setText("Last backup: " + time);
        } else {
            tvLastBackup.setText("No backup yet");
        }
    }
}
