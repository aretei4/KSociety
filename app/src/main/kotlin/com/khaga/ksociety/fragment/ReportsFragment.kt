package com.khaga.ksociety.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.khaga.ksociety.adapter.ReportAdapter
import com.khaga.ksociety.databinding.FragmentReportsBinding
import com.khaga.ksociety.database.AppDatabase
import com.khaga.ksociety.database.ReportDao
import com.khaga.ksociety.model.MonthlyReport
import com.khaga.ksociety.viewmodel.FundDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FundDetailViewModel
    private lateinit var adapter: ReportAdapter
    private var fundId = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fundId = arguments?.getLong("fund_id") ?: -1L
        viewModel = ViewModelProvider(requireParentFragment())[FundDetailViewModel::class.java]

        // Pass a callback so the adapter knows which month is "current"
        val currentMonth = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        adapter = ReportAdapter(currentMonth)

        binding.rvReports.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ReportsFragment.adapter
            isNestedScrollingEnabled = false
        }

        viewModel.reports.observe(viewLifecycleOwner) { reports ->
            _binding ?: return@observe
            // Sort: put current month first, rest descending
            val sorted = reports.sortedWith(
                compareByDescending<MonthlyReport> { it.month == currentMonth }
                    .thenByDescending { it.id }
            )
            adapter.submitList(sorted)
            // Auto-expand the first (current) item
            adapter.setExpandedPosition(0)
        }
    }

    override fun onDestroyView() { binding.rvReports.adapter = null; super.onDestroyView(); _binding = null }
}
