package com.khaga.ksociety.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.khaga.ksociety.activity.MainActivity
import com.khaga.ksociety.adapter.FundAdapter
import com.khaga.ksociety.databinding.FragmentHomeBinding
import com.khaga.ksociety.viewmodel.HomeViewModel
import java.util.Calendar

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: FundAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[HomeViewModel::class.java]

        adapter = FundAdapter { fund ->
            // Open fund — show BottomNav
            (requireActivity() as MainActivity).showBottomNav()
            val detail = FundDetailFragment().apply {
                arguments = Bundle().apply { putLong("fund_id", fund.id) }
            }
            (requireActivity() as MainActivity).navigateTo(detail, "fund_detail")
        }
        binding.rvFunds.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFunds.adapter = adapter

        binding.fabNewFund.setOnClickListener {
            (requireActivity() as MainActivity).navigateTo(AddFundFragment(), "add_fund")
        }
        binding.btnGotoBackup.setOnClickListener {
            (requireActivity() as MainActivity).navigateTo(BackupFragment(), "backup")
        }

        setupObservers()
        setGreeting()
    }

    override fun onResume() {
        super.onResume()
        // Always hide BottomNav when on the home/fund-list screen
        (requireActivity() as MainActivity).hideBottomNav()
        refresh()
    }

    fun refresh() = viewModel.loadAll()

    private fun setupObservers() {
        viewModel.funds.observe(viewLifecycleOwner) { funds ->
            _binding ?: return@observe
            adapter.submitList(funds)
            binding.layoutEmpty.visibility = if (funds.isEmpty()) View.VISIBLE else View.GONE
            binding.rvFunds.visibility     = if (funds.isEmpty()) View.GONE   else View.VISIBLE
        }
        viewModel.activeCount.observe(viewLifecycleOwner)  { _binding?.tvTotalFunds?.text   = it.toString() }
        viewModel.memberCount.observe(viewLifecycleOwner)  { _binding?.tvTotalMembers?.text = it.toString() }
        viewModel.overdueCount.observe(viewLifecycleOwner) { _binding?.tvOverdue?.text      = it.toString() }
    }

    private fun setGreeting() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when {
            hour < 12 -> "Good Morning \uD83D\uDC4B"
            hour < 17 -> "Good Afternoon \uD83D\uDC4B"
            else      -> "Good Evening \uD83D\uDC4B"
        }
    }

    override fun onDestroyView() { binding.rvFunds.adapter = null; super.onDestroyView(); _binding = null }
}
