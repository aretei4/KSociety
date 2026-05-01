package com.khaga.ksociety.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.khaga.ksociety.R
import com.khaga.ksociety.databinding.FragmentMemberPaymentDetailBinding
import com.khaga.ksociety.model.Member
import com.khaga.ksociety.model.Payment
import com.khaga.ksociety.util.CurrencyUtil
import com.khaga.ksociety.viewmodel.FundDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MemberPaymentDetailFragment : Fragment() {

    private var _binding: FragmentMemberPaymentDetailBinding? = null
    private val binding get() = _binding!!

    var member: Member? = null
    private lateinit var viewModel: FundDetailViewModel
    private var parsedMember: Member? = null
    private var parsedPayments: List<Payment> = emptyList()
    private var fundId = -1L

    companion object {
        fun newInstance(member: Member, memberPayments: List<Payment>,
                        allPayments: List<Payment>, fundId: Long
        ) = MemberPaymentDetailFragment().apply {
            val gson = Gson()
            arguments = bundleOf(
                "member_json"   to gson.toJson(member),
                "payments_json" to gson.toJson(memberPayments),
                "fund_id"       to fundId
            )
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMemberPaymentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gson = Gson()
        val memberJson = arguments?.getString("member_json") ?: return
        val payJson    = arguments?.getString("payments_json") ?: "[]"
        fundId         = arguments?.getLong("fund_id") ?: -1L
        parsedMember   = gson.fromJson(memberJson, Member::class.java)
        parsedPayments = gson.fromJson(payJson, object : TypeToken<List<Payment>>() {}.type)
        val m = parsedMember ?: return

        viewModel = ViewModelProvider(requireActivity())[FundDetailViewModel::class.java]

        binding.btnBackDetail.setOnClickListener { requireActivity().onBackPressed() }
        binding.tvDetailTitle.text = m.name

        val firstName = m.name.split(" ").firstOrNull() ?: m.name
        binding.btnRecordPayment.text = "💳  Record Payment for $firstName"
        binding.btnRecordPayment.setOnClickListener { showPaymentDialog(m) }
        binding.fabAddPayment.setOnClickListener { showPaymentDialog(m) }

        // Active loan bar
        if (m.amtBorrowed > 0) {
            binding.layoutActiveLoanBar.visibility = View.VISIBLE
            val minInt = (m.amtBorrowed * 0.02).toLong()
            binding.tvActiveLoanBar.text =
                "${CurrencyUtil.format(m.amtBorrowed)} · Int ${CurrencyUtil.format(minInt)}/mo"
        }

        bindStats(m)
        bindOutstandingAlert(m, parsedPayments)
        buildHistory(m, parsedPayments)

        viewModel.payments.observe(viewLifecycleOwner) { fresh ->
            _binding ?: return@observe
            val fm = parsedMember ?: return@observe
            val mp = fresh.filter { it.memberId == fm.id }
            parsedPayments = mp
            bindOutstandingAlert(fm, mp)
            buildHistory(fm, mp)
        }
    }

    private fun bindStats(m: Member) {
        binding.tvContribution.text = CurrencyUtil.format(m.contribution)
        binding.tvActiveLoan.text   = CurrencyUtil.format(m.amtBorrowed)
        binding.tvMinInterest.text  = CurrencyUtil.format((m.amtBorrowed * 0.02).toLong())
    }

    private fun bindOutstandingAlert(m: Member, payments: List<Payment>) {
        _binding ?: return
        val minInt       = (m.amtBorrowed * 0.02).toLong()
        val paidInterest = payments.filter { it.type.contains("Interest") && it.status == "paid" }.sumOf { it.interest }
        val outstanding  = (minInt - paidInterest).coerceAtLeast(0L)
        if (outstanding > 0) {
            binding.layoutOutstandingAlert.visibility = View.VISIBLE
            binding.tvOutstandingLabel.text = "Outstanding balance: ${CurrencyUtil.format(outstanding)}"
        } else {
            binding.layoutOutstandingAlert.visibility = View.GONE
        }
    }

    private fun showPaymentDialog(m: Member) {
        val sheet = PaymentFlowBottomSheet.newInstance(fundId)
        sheet.members        = listOf(m)
        sheet.selectedMember = m
        sheet.onPaymentSaved = { payment ->
            viewModel.insertPayment(payment) { success ->
                activity?.runOnUiThread {
                    _binding ?: return@runOnUiThread
                    Toast.makeText(requireContext(),
                        if (success) "✅ ${CurrencyUtil.format(payment.amount)} saved!" else "Failed.",
                        Toast.LENGTH_SHORT).show()
                    if (success) viewModel.loadPayments(fundId)
                }
            }
        }
        sheet.show(childFragmentManager, "payment_flow")
    }

    // ── Payment history ────────────────────────────────────────────────────────
    private fun buildHistory(m: Member, payments: List<Payment>) {
        val ctx       = requireContext()
        val container = _binding?.layoutHistory ?: return
        container.removeAllViews()
        val dp = ctx.resources.displayMetrics.density

        if (payments.isEmpty()) {
            container.addView(TextView(ctx).apply {
                text = "No payment history yet."
                textSize = 13f
                setTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.topMargin = (8 * dp).toInt() }
            })
            return
        }

        val yearStr      = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())
        val currentMonth = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date())

        val grouped = payments
            .groupBy { p ->
                val parts = p.date.split(" ")
                if (parts.size >= 2) "${parts[0]} $yearStr" else currentMonth
            }
            .entries.sortedByDescending { it.key }

        grouped.forEach { (month, monthPayments) ->
            val totalPaid = monthPayments.sumOf { it.amount }
            val recordedDate = monthPayments.firstOrNull()?.date ?: ""
            val allPaid = monthPayments.all { it.status == "paid" }

            // Card
            val card = LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                background  = ContextCompat.getDrawable(ctx, R.drawable.bg_card)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.bottomMargin = (10 * dp).toInt() }
            }

            // ── Header row ─────────────────────────────────────────────────────
            val headerRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = Gravity.CENTER_VERTICAL
                setPadding((14*dp).toInt(), (14*dp).toInt(), (14*dp).toInt(), (10*dp).toInt())
            }

            // Checkbox icon
            headerRow.addView(LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    (42*dp).toInt(), (42*dp).toInt()).also { it.marginEnd = (12*dp).toInt() }
                background = ContextCompat.getDrawable(ctx,
                    if (allPaid) R.drawable.bg_stat_card_green else R.drawable.bg_card)
                addView(TextView(ctx).apply {
                    text = if (allPaid) "✓" else "○"; textSize = 16f; gravity = Gravity.CENTER
                    setTextColor(ContextCompat.getColor(ctx,
                        if (allPaid) R.color.color_green else R.color.text_muted))
                })
            })

            // Month + recorded info
            headerRow.addView(LinearLayout(ctx).apply {
                orientation  = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                addView(TextView(ctx).apply {
                    text = month; textSize = 14f; setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
                })
                addView(TextView(ctx).apply {
                    text = "Recorded $recordedDate · ${getPaymentNote(monthPayments)}"
                    textSize = 11f
                    setTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
                })
            })

            // Total amount + swipe hint
            headerRow.addView(LinearLayout(ctx).apply {
                orientation = LinearLayout.VERTICAL; gravity = Gravity.END
                addView(TextView(ctx).apply {
                    text = CurrencyUtil.format(totalPaid); textSize = 15f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
                })
                addView(TextView(ctx).apply {
                    text = "← swipe to edit"; textSize = 10f
                    setTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
                })
            })

            card.addView(headerRow)

            // ── Type pills row ──────────────────────────────────────────────────
            val pillsRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = Gravity.CENTER_VERTICAL
                setPadding((14*dp).toInt(), 0, (14*dp).toInt(), (10*dp).toInt())
            }

            // Collect unique payment types from this month
            val typeMap = mutableMapOf<String, Long>()
            monthPayments.forEach { p ->
                if (p.principal > 0)  typeMap["Contribution"] = (typeMap["Contribution"] ?: 0) + p.principal
                if (p.interest  > 0)  typeMap["Interest"]     = (typeMap["Interest"]    ?: 0) + p.interest
                if (p.penalty   > 0)  typeMap["Penalty"]      = (typeMap["Penalty"]     ?: 0) + p.penalty
                if (p.memberFee > 0)  typeMap["Member Fee"]   = (typeMap["Member Fee"]  ?: 0) + p.memberFee
                // Fallback for older records
                if (p.principal == 0L && p.interest == 0L && p.penalty == 0L && p.memberFee == 0L) {
                    typeMap[p.type] = (typeMap[p.type] ?: 0) + p.amount
                }
            }

            val pillColors = mapOf(
                "Contribution" to Pair(R.color.color_green,  R.drawable.pill_green_bg),
                "Interest"     to Pair(R.color.color_blue,   R.drawable.bg_stat_card_blue),
                "Penalty"      to Pair(R.color.color_red,    R.drawable.bg_alert_red),
                "Member Fee"   to Pair(R.color.color_orange, R.drawable.pill_orange_bg),
            )

            typeMap.entries.take(4).forEach { (type, amount) ->
                val (colorRes, bgRes) = pillColors[type]
                    ?: Pair(R.color.text_muted, R.drawable.bg_card)
                pillsRow.addView(TextView(ctx).apply {
                    text     = "$type ${CurrencyUtil.format(amount)}"
                    textSize = 10f; setTypeface(null, Typeface.BOLD)
                    setTextColor(ContextCompat.getColor(ctx, colorRes))
                    background = ContextCompat.getDrawable(ctx, bgRes)
                    setPadding((8*dp).toInt(), (4*dp).toInt(), (8*dp).toInt(), (4*dp).toInt())
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).also { it.marginEnd = (6*dp).toInt(); it.bottomMargin = (6*dp).toInt() }
                })
            }
            card.addView(pillsRow)

            // ── Edit / Delete row ───────────────────────────────────────────────
            val divider = View(ctx).apply {
                setBackgroundColor(ContextCompat.getColor(ctx, R.color.divider_color))
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            }
            card.addView(divider)

            val actionsRow = LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                weightSum   = 2f
            }
            // Edit button
            actionsRow.addView(TextView(ctx).apply {
                text    = "✏ Edit"; textSize = 13f; gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(ctx, R.color.color_blue))
                layoutParams = LinearLayout.LayoutParams(0, (44*dp).toInt(), 1f)
                setOnClickListener { showEditPaymentDialog(monthPayments.firstOrNull(), m) }
            })
            // Divider between Edit and Delete
            actionsRow.addView(View(ctx).apply {
                setBackgroundColor(ContextCompat.getColor(ctx, R.color.divider_color))
                layoutParams = LinearLayout.LayoutParams(1, LinearLayout.LayoutParams.MATCH_PARENT)
            })
            // Delete button
            actionsRow.addView(TextView(ctx).apply {
                text    = "🗑 Delete"; textSize = 13f; gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(ctx, R.color.color_red))
                layoutParams = LinearLayout.LayoutParams(0, (44*dp).toInt(), 1f)
                setOnClickListener { confirmDeletePayments(monthPayments, month) }
            })

            card.addView(actionsRow)
            container.addView(card)
        }
    }

    private fun getPaymentNote(payments: List<Payment>): String {
        val notes = payments.mapNotNull { if (it.note.isNotBlank()) it.note else null }
        return if (notes.isNotEmpty()) notes.first() else "· ${SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())}"
    }

    // ── Edit payment dialog ────────────────────────────────────────────────────
    private fun showEditPaymentDialog(payment: Payment?, m: Member) {
        if (payment == null) return
        val sheet = PaymentFlowBottomSheet.newInstance(fundId)
        sheet.members         = listOf(m)
        sheet.selectedMember  = m
        sheet.existingPayment = payment
        sheet.onPaymentSaved = { updated ->
            val onDone: (Boolean) -> Unit = { success ->
                activity?.runOnUiThread {
                    _binding ?: return@runOnUiThread
                    Toast.makeText(requireContext(),
                        if (success) "✅ Updated!" else "Failed.", Toast.LENGTH_SHORT).show()
                    if (success) viewModel.loadPayments(fundId)
                }
            }
            if (updated.id > 0L) viewModel.updatePayment(updated, onDone)
            else viewModel.insertPayment(updated, onDone)
        }
        sheet.show(childFragmentManager, "edit_payment")
    }

    // ── Delete payment confirmation ────────────────────────────────────────────
    private fun confirmDeletePayments(payments: List<Payment>, month: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete $month payments?")
            .setMessage("This will permanently delete ${payments.size} payment record(s) totalling ${CurrencyUtil.format(payments.sumOf { it.amount })}.")
            .setPositiveButton("Delete") { _, _ ->
                payments.forEach { p ->
                    viewModel.deletePayment(p.id) { }
                }
                Toast.makeText(requireContext(), "Payments deleted.", Toast.LENGTH_SHORT).show()
                viewModel.loadPayments(fundId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}