package com.khaga.ksociety.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.khaga.ksociety.R
import com.khaga.ksociety.activity.MainActivity
import com.khaga.ksociety.adapter.PaymentAdapter
import com.khaga.ksociety.databinding.FragmentDashboardBinding
import com.khaga.ksociety.util.CurrencyUtil
import com.khaga.ksociety.viewmodel.FundDetailViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FundDetailViewModel
    private lateinit var recentAdapter: PaymentAdapter
    private var fundId = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fundId    = arguments?.getLong("fund_id") ?: -1L
        viewModel = ViewModelProvider(requireParentFragment())[FundDetailViewModel::class.java]

        recentAdapter = PaymentAdapter()
        binding.rvRecentPayments.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter       = recentAdapter
            isNestedScrollingEnabled = false
        }

        setupObservers()
        setupQuickActions()
    }

    private fun setupObservers() {
        viewModel.fund.observe(viewLifecycleOwner) { fund ->
            _binding ?: return@observe
            fund ?: return@observe
            binding.tvThisMonth.text = CurrencyUtil.format(fund.monthlyAmount)
            binding.layoutOverdueAlert.visibility =
                if (fund.isOverdue && fund.overdueCount > 0) View.VISIBLE else View.GONE
            if (fund.isOverdue)
                binding.tvOverdueTitle.text = "${fund.overdueCount} payment(s) overdue"
        }
        viewModel.reports.observe(viewLifecycleOwner) { reports ->
            _binding ?: return@observe
            val latest = reports.maxByOrNull { it.id }
            binding.tvTotalPool.text     = CurrencyUtil.format(latest?.totalFund  ?: 0L)
            binding.tvInterestMonth.text = CurrencyUtil.format(latest?.interestIn ?: 0L)
        }
        viewModel.payments.observe(viewLifecycleOwner) { payments ->
            _binding ?: return@observe
            recentAdapter.submitList(payments.take(5))
        }
    }

    private fun setupQuickActions() {
        // Collect Payment — opens the 3-step PaymentFlowBottomSheet directly
        binding.btnCollectPayment.setOnClickListener {
            openPaymentFlow()
        }

        // Give Loan — open GiveLoanBottomSheet directly
        binding.btnGiveLoan.setOnClickListener {
            openGiveLoanSheet()
        }

        // Add Member — navigate to AddMemberFragment
        binding.btnAddMember.setOnClickListener {
            val f = AddMemberFragment().apply {
                arguments = Bundle().apply { putLong("fund_id", fundId) }
            }
            (requireActivity() as MainActivity).navigateTo(f, "add_member")
            (requireActivity() as MainActivity).hideBottomNav()
        }
    }

    private fun openPaymentFlow() {
        val members = viewModel.members.value.orEmpty()
        if (members.isEmpty()) {
            android.widget.Toast.makeText(
                requireContext(), "No members yet. Add members first.", android.widget.Toast.LENGTH_SHORT
            ).show()
            return
        }
        val sheet = PaymentFlowBottomSheet.newInstance(fundId)
        sheet.members = members
        sheet.onPaymentSaved = { payment ->
            viewModel.insertPayment(payment) { success ->
                activity?.runOnUiThread {
                    _binding ?: return@runOnUiThread
                    android.widget.Toast.makeText(
                        requireContext(),
                        if (success) "\u2705 \u20b9${payment.amount} saved!" else "Failed. Try again.",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        sheet.show(childFragmentManager, "payment_flow_dashboard")
    }

    private fun openGiveLoanSheet() {
        val members = viewModel.members.value.orEmpty()
        if (members.isEmpty()) {
            android.widget.Toast.makeText(
                requireContext(), "No members yet. Add members first.",
                android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val sheet = GiveLoanBottomSheet.newInstance()
        sheet.members = members
        sheet.onLoanIssued = { memberId, amount, _, _ ->
            val member = members.find { it.id == memberId }
            if (member != null) {
                val updated = member.copy(amtBorrowed = member.amtBorrowed + amount)
                viewModel.updateMember(updated) { success ->
                    activity?.runOnUiThread {
                        _binding ?: return@runOnUiThread
                        android.widget.Toast.makeText(requireContext(),
                            if (success) "\uD83D\uDCB8 Loan issued!" else "Failed.",
                            android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        sheet.show(childFragmentManager, "give_loan_dashboard")
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
