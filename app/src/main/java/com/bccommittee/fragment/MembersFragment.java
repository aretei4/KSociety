package com.bccommittee.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bccommittee.R;
import com.bccommittee.activity.AddMemberActivity;
import com.bccommittee.adapter.MemberAdapter;
import com.bccommittee.database.DatabaseHelper;
import com.bccommittee.model.Fund;
import com.bccommittee.model.Member;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.util.List;

public class MembersFragment extends Fragment implements MemberAdapter.OnMemberClickListener {

    private long fundId;
    private DatabaseHelper db;
    private MemberAdapter adapter;
    private LinearLayout layoutEmpty;
    private EditText etSearch;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_members, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) fundId = getArguments().getLong("fund_id", -1);
        db = DatabaseHelper.getInstance(requireContext());

        Fund fund = db.getFundById(fundId);

        RecyclerView rv = view.findViewById(R.id.rv_members);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new MemberAdapter(requireContext(), this);
        rv.setAdapter(adapter);

        layoutEmpty = view.findViewById(R.id.layout_empty);

        // Search
        etSearch = view.findViewById(R.id.et_search);
        ImageButton btnClear = view.findViewById(R.id.btn_clear_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                filterMembers(s.toString());
            }
        });
        btnClear.setOnClickListener(v -> etSearch.setText(""));

        // FAB
        ExtendedFloatingActionButton fab = view.findViewById(R.id.fab_add_member);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddMemberActivity.class);
            intent.putExtra("fund_id", fundId);
            if (fund != null) intent.putExtra("fund_name", fund.getName());
            startActivity(intent);
        });

        loadMembers();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMembers();
    }

    private void loadMembers() {
        String query = etSearch != null ? etSearch.getText().toString() : "";
        filterMembers(query);
    }

    private void filterMembers(String query) {
        List<Member> list;
        if (query.isEmpty()) {
            list = db.getMembersByFund(fundId);
        } else {
            list = db.searchMembers(fundId, query);
        }
        adapter.setMembers(list);
        layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onMemberClick(Member member) {
        // Show member detail dialog / bottom sheet
        Toast.makeText(requireContext(),
            member.getName() + " — Loan: ₹" + member.getAmtBorrowed(),
            Toast.LENGTH_SHORT).show();
    }
}
