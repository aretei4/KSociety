package com.khaga.ksociety.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.*
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.khaga.ksociety.R
import com.khaga.ksociety.databinding.DialogRecordPaymentStepsBinding
import com.khaga.ksociety.databinding.FragmentPaymentsBinding
import com.khaga.ksociety.databinding.ItemMemberPaymentBinding
import com.khaga.ksociety.model.Member
import com.khaga.ksociety.model.Payment
import com.khaga.ksociety.util.CurrencyUtil
import com.khaga.ksociety.viewmodel.FundDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentsFragment : Fragment() {

    private var _binding: FragmentPaymentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FundDetailViewModel
    private lateinit var memberAdapter: MemberPaymentAdapter
    private var fundId       = -1L
    private var allMembers   = listOf<Member>()
    private var allPayments  = listOf<Payment>()
    private var activeFilter = "all"
    private var searchQuery  = ""

    private val mainHandler  = Handler(Looper.getMainLooper())
    private val applyRunnable = Runnable { applyFilterNow() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPaymentsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fundId    = arguments?.getLong("fund_id") ?: -1L
        viewModel = ViewModelProvider(requireParentFragment())[FundDetailViewModel::class.java]

        memberAdapter = MemberPaymentAdapter { member -> openMemberDetail(member) }
        binding.rvPayments.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPayments.adapter        = memberAdapter
        binding.rvPayments.setHasFixedSize(false)

        setupSearch()
        setupChips()
        setupObservers()
    }

    // ── Search ────────────────────────────────────────────────────────────────
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                _binding ?: return
                searchQuery = s?.toString().orEmpty()
                binding.btnClearSearch.visibility =
                    if (searchQuery.isNotEmpty()) View.VISIBLE else View.GONE
                scheduleApplyFilter()
            }
        })
        binding.btnClearSearch.setOnClickListener { binding.etSearch.setText("") }
    }

    // ── Chips ─────────────────────────────────────────────────────────────────
    private fun setupChips() {
        binding.chipAll.isChecked = true
        binding.chipAll.setOnClickListener      { activeFilter = "all";       refreshChips(); scheduleApplyFilter() }
        binding.chipPaid.setOnClickListener     { activeFilter = "clear";     refreshChips(); scheduleApplyFilter() }
        binding.chipPending.setOnClickListener  { activeFilter = "partial";   refreshChips(); scheduleApplyFilter() }
        binding.chipInterest.setOnClickListener { activeFilter = "defaulted"; refreshChips(); scheduleApplyFilter() }
    }

    private fun refreshChips() {
        _binding ?: return
        binding.chipAll.isChecked      = activeFilter == "all"
        binding.chipPaid.isChecked     = activeFilter == "clear"
        binding.chipPending.isChecked  = activeFilter == "partial"
        binding.chipInterest.isChecked = activeFilter == "defaulted"
    }

    // ── Observers ─────────────────────────────────────────────────────────────
    private fun setupObservers() {
        viewModel.fund.observe(viewLifecycleOwner) { fund ->
            _binding ?: return@observe
            fund ?: return@observe
            binding.tvHeaderSubtitle.text =
                "${fund.name} · ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())}"
        }
        viewModel.members.observe(viewLifecycleOwner) { members ->
            _binding ?: return@observe
            allMembers = members
            updateStats()
            scheduleApplyFilter()
        }
        viewModel.payments.observe(viewLifecycleOwner) { payments ->
            _binding ?: return@observe
            allPayments = payments
            updateStats()
            scheduleApplyFilter()
        }
    }

    private fun scheduleApplyFilter() {
        mainHandler.removeCallbacks(applyRunnable)
        mainHandler.postDelayed(applyRunnable, 50L)
    }

    private fun updateStats() {
        _binding ?: return
        val mon = SimpleDateFormat("MMM", Locale.getDefault()).format(Date())
        var clear = 0; var outstanding = 0
        allMembers.forEach { m ->
            val mp   = allPayments.filter { it.memberId == m.id }
            val paid = mp.any { it.date.startsWith(mon) && it.status == "paid" }
            if (paid) clear++ else outstanding++
        }
        binding.tvStatTotal.text       = allMembers.size.toString()
        binding.tvStatClear.text       = clear.toString()
        binding.tvStatOutstanding.text = outstanding.toString()
    }

    private fun applyFilterNow() {
        val b = _binding ?: return
        if (b.rvPayments.layoutManager == null) return
        val mon      = SimpleDateFormat("MMM", Locale.getDefault()).format(Date())
        val filtered = allMembers
            .filter { m ->
                searchQuery.isBlank() ||
                m.name.contains(searchQuery, ignoreCase = true) ||
                m.phone.contains(searchQuery, ignoreCase = true)
            }
            .filter { m ->
                val mp = allPayments.filter { it.memberId == m.id }
                when (activeFilter) {
                    "clear"     -> mp.any { it.date.startsWith(mon) && it.status == "paid" }
                    "partial"   -> !mp.any { it.date.startsWith(mon) && it.status == "paid" } && mp.isNotEmpty()
                    "defaulted" -> !mp.any { it.date.startsWith(mon) && it.status == "paid" } && mp.isEmpty()
                    else        -> true
                }
            }
        memberAdapter.submitList(
            filtered.map { MemberPaymentItem(it, allPayments.filter { p -> p.memberId == it.id }) }
        )
        b.layoutEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        b.rvPayments.visibility  = if (filtered.isEmpty()) View.GONE   else View.VISIBLE
    }

    // ── Open member detail ─────────────────────────────────────────────────────
    private fun openMemberDetail(member: Member) {
        val mp   = allPayments.filter { it.memberId == member.id }
        val frag = MemberPaymentDetailFragment.newInstance(member, mp, allPayments, fundId)
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, frag, "member_payment_detail")
            .addToBackStack("member_payment_detail")
            .commit()
    }

    // ── Payment flow (PaymentFlowBottomSheet) ─────────────────────────────────
    private fun showPaymentFlow(member: Member) {
        val sheet = PaymentFlowBottomSheet.newInstance(fundId)
        sheet.members        = viewModel.members.value.orEmpty()
        sheet.selectedMember = member
        sheet.onPaymentSaved = { payment ->
            viewModel.insertPayment(payment) { success ->
                mainHandler.post {
                    if (_binding == null) return@post
                    Toast.makeText(requireContext(),
                        if (success) "✅ ₹${payment.amount} saved!" else "Failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
        sheet.show(childFragmentManager, "payment_flow")
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPayments(fundId)
        viewModel.loadMembers(fundId)
    }

    override fun onDestroyView() {
        mainHandler.removeCallbacks(applyRunnable)
        binding.rvPayments.adapter = null
        super.onDestroyView()
        _binding = null
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Data + Adapter — uses ItemMemberPaymentBinding
    // ══════════════════════════════════════════════════════════════════════════
    data class MemberPaymentItem(val member: Member, val payments: List<Payment>)

    private val MEMBER_DIFF = object : DiffUtil.ItemCallback<MemberPaymentItem>() {
        override fun areItemsTheSame(o: MemberPaymentItem, n: MemberPaymentItem) =
            o.member.id == n.member.id
        override fun areContentsTheSame(o: MemberPaymentItem, n: MemberPaymentItem) = o == n
    }

    inner class MemberPaymentAdapter(
        private val onClick: (Member) -> Unit
    ) : ListAdapter<MemberPaymentItem, MemberPaymentAdapter.VH>(MEMBER_DIFF) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            ItemMemberPaymentBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )

        override fun onBindViewHolder(holder: VH, position: Int) =
            holder.bind(getItem(position))

        inner class VH(private val b: ItemMemberPaymentBinding) :
            RecyclerView.ViewHolder(b.root) {

            fun bind(item: MemberPaymentItem) {
                val m   = item.member
                val mp  = item.payments
                val ctx = itemView.context
                val dp  = ctx.resources.displayMetrics.density

                b.tvAvatar.text    = m.avatar
                b.tvMemberName.text = m.name
                b.tvPhoneDate.text  = buildString {
                    if (m.phone.isNotBlank()) append("${m.phone} · ")
                    append(m.joinDate)
                }

                // Outstanding interest
                val minInt       = (m.amtBorrowed * 0.02).toLong()
                val paidInterest = mp.filter { it.type.contains("Interest") && it.status == "paid" }
                                     .sumOf { it.interest }
                val outstanding  = if (m.amtBorrowed > 0)
                    (minInt - paidInterest).coerceAtLeast(0L) else 0L

                if (outstanding > 0) {
                    b.tvOutstandingAmount.text = "-${CurrencyUtil.format(outstanding)}"
                    b.tvOutstandingAmount.setTextColor(
                        ContextCompat.getColor(ctx, R.color.color_red))
                    b.tvStatusLabel.text = "outstanding"
                } else {
                    b.tvOutstandingAmount.text = "Clear"
                    b.tvOutstandingAmount.setTextColor(
                        ContextCompat.getColor(ctx, R.color.color_green))
                    b.tvStatusLabel.text = ""
                }

                // Health bar
                val healthPct = if (m.amtBorrowed > 0 && minInt > 0)
                    ((paidInterest.toFloat() / minInt) * 100).toInt().coerceIn(0, 100)
                else 100
                b.tvHealthPct.text     = "$healthPct%"
                b.progressHealth.progress = healthPct

                // Month pills
                val currentMonth = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())
                b.tvMonthLabel.text = currentMonth
                b.layoutPills.removeAllViews()
                listOf("Contribution", "Interest", "Fee").forEach { type ->
                    val paid = mp.any { it.type.contains(type) && it.status == "paid" }
                    b.layoutPills.addView(TextView(ctx).apply {
                        text    = if (paid) "✓" else "·"
                        textSize = 12f
                        gravity = Gravity.CENTER
                        setTextColor(ContextCompat.getColor(ctx,
                            if (paid) R.color.color_green else R.color.text_muted))
                        if (paid) background = ContextCompat.getDrawable(
                            ctx, R.drawable.bg_pill_green_check)
                        layoutParams = LinearLayout.LayoutParams(
                            (24 * dp).toInt(), (24 * dp).toInt()
                        ).also { it.marginEnd = (4 * dp).toInt() }
                    })
                }

                // Paid status pill
                val hasPaid = mp.any { it.status == "paid" }
                b.tvPaidStatus.text = if (hasPaid) "Paid" else "Pending"
                b.tvPaidStatus.background = ContextCompat.getDrawable(ctx,
                    if (hasPaid) R.drawable.pill_green_bg else R.drawable.pill_orange_bg)
                b.tvPaidStatus.setTextColor(ContextCompat.getColor(ctx,
                    if (hasPaid) R.color.color_green else R.color.color_orange))

                b.root.setOnClickListener { onClick(m) }
            }
        }
    }
}
