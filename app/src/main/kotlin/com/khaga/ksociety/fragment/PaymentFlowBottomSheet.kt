package com.khaga.ksociety.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.text.*
import android.view.*
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.khaga.ksociety.R
import com.khaga.ksociety.databinding.DialogPaymentStep1Binding
import com.khaga.ksociety.databinding.DialogPaymentStep2Binding
import com.khaga.ksociety.databinding.DialogPaymentStep3Binding
import com.khaga.ksociety.databinding.ItemMemberStep1Binding
import com.khaga.ksociety.model.Member
import com.khaga.ksociety.model.Payment
import com.khaga.ksociety.util.CurrencyUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentFlowBottomSheet : BottomSheetDialogFragment() {

    var members: List<Member> = emptyList()
    var selectedMember: Member? = null
    var existingPayment: Payment? = null
    var onPaymentSaved: ((Payment) -> Unit)? = null
    var fundId = -1L

    companion object {
        fun newInstance(fundId: Long) = PaymentFlowBottomSheet().apply { this.fundId = fundId }
    }

    // ── Step bindings — held so we can access fields across methods ──────────
    private var _b1: DialogPaymentStep1Binding? = null
    private var _b2: DialogPaymentStep2Binding? = null
    private var _b3: DialogPaymentStep3Binding? = null

    override fun onStart() {
        super.onStart()
        // Expand to full height so Continue button is always visible
        val d = dialog as? BottomSheetDialog ?: return
        val sheet = d.findViewById<android.view.View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        val behavior = BottomSheetBehavior.from(sheet)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        // Allow the sheet to be as tall as the screen
        sheet.layoutParams?.height = android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        sheet.requestLayout()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b1 = DialogPaymentStep1Binding.inflate(inflater, container, false)
        return _b1!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindStep1()
    }

    override fun onDestroyView() {
        _b1 = null; _b2 = null; _b3 = null
        super.onDestroyView()
    }

    // ══════════════════════════════════════════════════════════════════════
    // STEP 1 — Select Member
    // ══════════════════════════════════════════════════════════════════════
    private fun bindStep1() {
        val b = _b1 ?: return
        b.btnClose.setOnClickListener { dismiss() }

        val adapter = MemberSelectAdapter(selectedMember) { member ->
            selectedMember = member
            b.btnContinue.isEnabled = true
            b.btnContinue.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    ContextCompat.getColor(requireContext(), R.color.header_green))
            b.btnContinue.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.white))
        }

        b.rvMembers.layoutManager = LinearLayoutManager(requireContext())
        b.rvMembers.adapter = adapter
        adapter.submitList(members)

        b.etSearchMember.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b2: Int, c: Int) {
                val q = s?.toString().orEmpty()
                b.btnClearSearch.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
                adapter.submitList(
                    members.filter {
                        it.name.contains(q, ignoreCase = true) ||
                                it.phone.contains(q, ignoreCase = true)
                    }
                )
            }
        })
        b.btnClearSearch.setOnClickListener { b.etSearchMember.setText("") }

        b.btnContinue.setOnClickListener {
            val m = selectedMember ?: return@setOnClickListener
            swapToStep2(m)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // STEP 2 — Enter Amounts
    // ══════════════════════════════════════════════════════════════════════
    private fun swapToStep2(member: Member) {
        val inflater = LayoutInflater.from(requireContext())
        _b2 = DialogPaymentStep2Binding.inflate(inflater)
        swapContent(_b2!!.root)
        bindStep2(member)
    }

    private fun bindStep2(member: Member) {
        val b      = _b2 ?: return
        val ctx    = requireContext()
        val dp     = ctx.resources.displayMetrics.density
        val minInt = (member.amtBorrowed * 0.02).toLong()
        val hasLoan = member.amtBorrowed > 0

        b.btnClose.setOnClickListener { dismiss() }
        b.tvMemberAvatar.text    = member.avatar
        b.tvMemberName.text      = member.name
        b.tvOutstandingInfo.text = if (hasLoan)
            "Outstanding ${CurrencyUtil.format(member.amtBorrowed)}" else "No active loan"
        b.btnChangeMember.setOnClickListener {
            _b2 = null
            swapContent(_b1!!.root)
            bindStep1()
        }

        b.tvPrincipalHint.text = if (hasLoan)
            "Outstanding ${CurrencyUtil.format(member.amtBorrowed)}" else "No active loan"
        b.tvInterestHint.text  = if (hasLoan)
            "Min due ${CurrencyUtil.format(minInt)} (2%/mo)" else "No loan — no interest"
        b.tvFeeHint.text       = if (member.fees > 0)
            "Standard fee ${CurrencyUtil.format(member.fees)}/mo" else "Monthly admin fee"

        if (!hasLoan) {
            b.cardPrincipal.alpha = 0.45f
            b.cardInterest.alpha  = 0.45f
            b.etPrincipal.isEnabled = false
            b.etInterest.isEnabled  = false
        } else {
            b.etInterest.setText(minInt.toString())
        }
        b.etPenalty.setText("0")
        if (member.fees > 0) b.etFee.setText(member.fees.toString())

        // Pre-fill from existing payment when editing
        existingPayment?.let { ep ->
            if (ep.principal > 0 && hasLoan) b.etPrincipal.setText(ep.principal.toString())
            if (ep.interest  > 0 && hasLoan) b.etInterest.setText(ep.interest.toString())
            if (ep.penalty   > 0)            b.etPenalty.setText(ep.penalty.toString())
            if (ep.memberFee > 0)            b.etFee.setText(ep.memberFee.toString())
        }

        // Quick-fill buttons
        if (hasLoan) {
            addQuickFill(b.layoutPrincipalQuickfill, listOf(1000L, 2000L, 5000L), b.etPrincipal, "#1A56A0", dp)
            addQuickFill(b.layoutInterestQuickfill,  listOf(minInt, minInt*2, minInt*3), b.etInterest, "#1F7A4A", dp)
        }
        if (member.fees > 0) {
            addQuickFill(b.layoutFeeQuickfill, listOf(member.fees), b.etFee, "#D4600A", dp)
        }

        b.btnBack.setOnClickListener {
            _b2 = null
            swapContent(_b1!!.root)
            bindStep1()
        }

        b.btnReview.setOnClickListener {
            val principal = b.etPrincipal.text.toString().toLongOrNull() ?: 0L
            val interest  = b.etInterest.text.toString().toLongOrNull()  ?: 0L
            val penalty   = b.etPenalty.text.toString().toLongOrNull()   ?: 0L
            val fee       = b.etFee.text.toString().toLongOrNull()       ?: 0L
            val total     = principal + interest + penalty + fee
            if (total == 0L) {
                Toast.makeText(ctx, "Enter at least one amount.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            swapToStep3(member, principal, interest, penalty, fee, total)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // STEP 3 — Review & Save
    // ══════════════════════════════════════════════════════════════════════
    private fun swapToStep3(
        member: Member,
        principal: Long, interest: Long, penalty: Long, fee: Long, total: Long
    ) {
        val inflater = LayoutInflater.from(requireContext())
        _b3 = DialogPaymentStep3Binding.inflate(inflater)
        swapContent(_b3!!.root)
        bindStep3(member, principal, interest, penalty, fee, total)
    }

    private fun bindStep3(
        member: Member,
        principal: Long, interest: Long, penalty: Long, fee: Long, total: Long
    ) {
        val b   = _b3 ?: return
        val ctx = requireContext()
        val dp  = ctx.resources.displayMetrics.density

        b.btnClose.setOnClickListener { dismiss() }
        b.tvMemberAvatar.text  = member.avatar
        b.tvMemberName.text    = member.name
        b.tvPaymentDate.text   = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())

        // Build breakdown rows programmatically
        b.layoutBreakdown.removeAllViews()

        data class Row(val icon: String, val label: String, val amount: Long, val color: String)
        val rows = buildList {
            if (principal > 0) add(Row("💵", "Contribution", principal, "#1A56A0"))
            if (interest  > 0) add(Row("📈", "Interest",      interest,  "#1F7A4A"))
            if (penalty   > 0) add(Row("⚠️", "Penalty",       penalty,   "#D4600A"))
            if (fee       > 0) add(Row("🪙", "Member Fee",    fee,       "#D4600A"))
        }

        rows.forEachIndexed { i, row ->
            if (i > 0) {
                b.layoutBreakdown.addView(View(ctx).apply {
                    setBackgroundColor(ContextCompat.getColor(ctx, R.color.divider_color))
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, 1)
                })
            }
            b.layoutBreakdown.addView(LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = Gravity.CENTER_VERTICAL
                setPadding((16*dp).toInt(), (16*dp).toInt(), (16*dp).toInt(), (16*dp).toInt())
                addView(TextView(ctx).apply {
                    text = "${row.icon}  ${row.label}"; textSize = 14f
                    setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
                addView(TextView(ctx).apply {
                    text = CurrencyUtil.format(row.amount); textSize = 16f
                    setTypeface(null, Typeface.BOLD)
                    setTextColor(android.graphics.Color.parseColor(row.color))
                })
            })
        }

        // Divider + Total row
        b.layoutBreakdown.addView(View(ctx).apply {
            setBackgroundColor(ContextCompat.getColor(ctx, R.color.divider_color))
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
        })
        b.layoutBreakdown.addView(LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            setPadding((16*dp).toInt(), (16*dp).toInt(), (16*dp).toInt(), (16*dp).toInt())
            addView(TextView(ctx).apply {
                text = "Total"; textSize = 15f; setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(ctx, R.color.text_primary))
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(TextView(ctx).apply {
                text = CurrencyUtil.format(total); textSize = 22f
                setTypeface(null, Typeface.BOLD)
                setTextColor(ContextCompat.getColor(ctx, R.color.header_green))
            })
        })

        b.btnEdit.setOnClickListener { swapToStep2(member) }

        b.btnConfirm.setOnClickListener {
            val types = buildList {
                if (principal > 0) add("Contribution")
                if (interest  > 0) add("Interest")
                if (penalty   > 0) add("Penalty")
                if (fee       > 0) add("Member Fee")
            }
            val payment = Payment(
                id           = existingPayment?.id ?: 0L,
                memberId     = member.id,
                memberName   = member.name,
                memberAvatar = member.avatar,
                type         = types.joinToString(" + "),
                amount       = total,
                principal    = principal,
                interest     = interest,
                penalty      = penalty,
                memberFee    = fee,
                date         = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date()),
                status       = "paid",
                fundId       = fundId
            )
            onPaymentSaved?.invoke(payment)
            dismiss()
        }
    }

    // ── Quick-fill buttons ────────────────────────────────────────────────────
    private fun addQuickFill(
        container: LinearLayout, amounts: List<Long>,
        targetEt: EditText, hexColor: String, dp: Float
    ) {
        container.removeAllViews()
        val ctx   = requireContext()
        val color = android.graphics.Color.parseColor(hexColor)
        amounts.filter { it > 0 }.distinct().take(3).forEach { amt ->
            val btn = TextView(ctx).apply {
                text      = CurrencyUtil.format(amt)
                textSize  = 11f
                setTypeface(null, Typeface.BOLD)
                setTextColor(color)
                gravity   = Gravity.CENTER
                background = ContextCompat.getDrawable(ctx, R.drawable.bg_quickfill_btn)
                setPadding((12*dp).toInt(), (7*dp).toInt(), (12*dp).toInt(), (7*dp).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.marginEnd = (8*dp).toInt() }
                setOnClickListener {
                    targetEt.setText(amt.toString())
                    targetEt.setSelection(targetEt.text.length)
                    // Highlight selected, reset others
                    for (i in 0 until container.childCount) {
                        val v = container.getChildAt(i) as? TextView ?: continue
                        v.background = ContextCompat.getDrawable(ctx, R.drawable.bg_quickfill_btn)
                        v.setTextColor(color)
                    }
                    background = ContextCompat.getDrawable(ctx, R.drawable.bg_quickfill_btn_selected)
                    setTextColor(ContextCompat.getColor(ctx, R.color.white))
                }
            }
            container.addView(btn)
        }
    }

    // ── Swap sheet content ────────────────────────────────────────────────────
    private fun swapContent(newView: View) {
        val decorView   = dialog?.window?.decorView as? ViewGroup ?: return
        val content     = decorView.findViewById<ViewGroup>(android.R.id.content) ?: return
        val coordinator = content.getChildAt(0) as? ViewGroup ?: return
        // Walk to the BottomSheet container
        val bottomSheet = findBottomSheet(coordinator) ?: return
        bottomSheet.removeAllViews()
        bottomSheet.addView(newView)
    }

    private fun findBottomSheet(vg: ViewGroup): ViewGroup? {
        for (i in 0 until vg.childCount) {
            val child = vg.getChildAt(i)
            if (child is ViewGroup) {
                if (child.id == com.google.android.material.R.id.design_bottom_sheet)
                    return child
                findBottomSheet(child)?.let { return it }
            }
        }
        return null
    }

    // ══════════════════════════════════════════════════════════════════════
    // Member select adapter — uses ItemMemberStep1Binding
    // ══════════════════════════════════════════════════════════════════════
    private inner class MemberSelectAdapter(
        private var currentSel: Member?,
        private val onSelect: (Member) -> Unit
    ) : ListAdapter<Member, MemberSelectAdapter.VH>(object : DiffUtil.ItemCallback<Member>() {
        override fun areItemsTheSame(o: Member, n: Member) = o.id == n.id
        override fun areContentsTheSame(o: Member, n: Member) = o == n
    }) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
            ItemMemberStep1Binding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
        override fun onBindViewHolder(holder: VH, position: Int) =
            holder.bind(getItem(position))

        inner class VH(private val b: ItemMemberStep1Binding) :
            RecyclerView.ViewHolder(b.root) {

            fun bind(m: Member) {
                val ctx      = itemView.context
                val selected = currentSel?.id == m.id

                b.tvAvatar.text   = m.avatar
                b.tvName.text     = m.name
                b.tvLoanInfo.text = if (m.amtBorrowed > 0) {
                    val minInt = (m.amtBorrowed * 0.02).toLong()
                    "Loan ${CurrencyUtil.format(m.amtBorrowed)} · Int ${CurrencyUtil.format(minInt)}/mo"
                } else "No active loan"

                b.tvNoLoan.visibility = View.GONE

                b.root.background = ContextCompat.getDrawable(ctx,
                    if (selected) R.drawable.bg_member_item_selected
                    else R.drawable.bg_member_item_unselected)
                b.ivRadio.setImageDrawable(ContextCompat.getDrawable(ctx,
                    if (selected) R.drawable.ic_radio_selected
                    else R.drawable.ic_radio_unselected))

                b.root.setOnClickListener {
                    val prevIdx = currentList.indexOfFirst { it.id == currentSel?.id }
                    currentSel = m
                    if (prevIdx >= 0) notifyItemChanged(prevIdx)
                    notifyItemChanged(bindingAdapterPosition)
                    onSelect(m)
                }
            }
        }
    }
}