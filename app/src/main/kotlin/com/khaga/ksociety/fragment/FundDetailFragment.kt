package com.khaga.ksociety.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.khaga.ksociety.activity.MainActivity
import com.khaga.ksociety.databinding.FragmentFundDetailBinding
import com.khaga.ksociety.viewmodel.FundDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FundDetailFragment : Fragment() {

    private var _binding: FragmentFundDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FundDetailViewModel
    private var fundId = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentFundDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fundId    = arguments?.getLong("fund_id") ?: -1L
        viewModel = ViewModelProvider(this)[FundDetailViewModel::class.java]
        viewModel.load(fundId)

        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }

        // ⋮ menu button — delete fund
        binding.btnMenu.setOnClickListener { showFundMenu() }

        viewModel.fund.observe(viewLifecycleOwner) { fund ->
            _binding ?: return@observe
            if (fund == null) { requireActivity().onBackPressed(); return@observe }
            binding.tvFundTitle.text    = fund.name
            binding.tvFundSubtitle.text =
                SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        }

        setupViewPager()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).showBottomNav()
    }

    private fun showFundMenu() {
        AlertDialog.Builder(requireContext())
            .setTitle("Fund Options")
            .setItems(arrayOf("Delete Fund")) { _, which ->
                if (which == 0) confirmDeleteFund()
            }.show()
    }

    private fun confirmDeleteFund() {
        val fundName = viewModel.fund.value?.name ?: "this fund"
        AlertDialog.Builder(requireContext())
            .setTitle("Delete $fundName?")
            .setMessage("This permanently deletes the fund, all members, and all payment records. This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteFund(fundId) { success ->
                    activity?.runOnUiThread {
                        if (success) {
                            Toast.makeText(requireContext(), "Fund deleted.", Toast.LENGTH_SHORT).show()
                            requireActivity().onBackPressed()
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete fund.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupViewPager() {
        val tabTitles = arrayOf("Dashboard", "Members", "Payments", "Reports")

        binding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 4
            override fun createFragment(position: Int): Fragment {
                val args = Bundle().apply { putLong("fund_id", fundId) }
                return when (position) {
                    1    -> MembersFragment()
                    2    -> PaymentsFragment()
                    3    -> ReportsFragment()
                    else -> DashboardFragment()
                }.apply { arguments = args }
            }
        }

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = tabTitles[pos]
        }.attach()
    }

    fun selectTab(index: Int) {
        _binding?.viewPager?.currentItem = index
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
