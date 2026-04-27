package com.khaga.ksociety.fragment

import android.graphics.Color
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
import com.khaga.ksociety.databinding.DialogLoanStep1Binding
import com.khaga.ksociety.databinding.DialogLoanStep2Binding
import com.khaga.ksociety.databinding.DialogLoanStep3Binding
import com.khaga.ksociety.databinding.ItemMemberStep1Binding
import com.khaga.ksociety.model.Member
import com.khaga.ksociety.util.CurrencyUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GiveLoanBottomSheet : BottomSheetDialogFragment() {

    var members: List<Member> = emptyList()
    var preSelectedMember: Member? = null
    var onLoanIssued: ((memberId: Long, amount: Long, rate: Float, purpose: String) -> Unit)? = null

    private var selectedMember: Member? = null
    private var _b1: DialogLoanStep1Binding? = null
    private var _b2: DialogLoanStep2Binding? = null
    private var _b3: DialogLoanStep3Binding? = null

    companion object {
        private val PURPOSES = listOf(
            "Personal Use", "Medical", "Business", "Education", "Home", "Other"
        )
        private val QUICK_AMOUNTS = listOf(5000L, 10000L, 25000L, 50000L)
        private val QUICK_RATES   = listOf(1f, 1.5f, 2f, 2.5f)

        fun newInstance() = GiveLoanBottomSheet()
    }

    override fun onStart() {
        super.onStart()
        val d     = dialog as? BottomSheetDialog ?: return
        val sheet = d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) ?: return
        BottomSheetBehavior.from(sheet).apply {
            state          = BottomSheetBehavior.STATE_EXPANDED
            skipCollapsed  = true
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b1 = DialogLoanStep1Binding.inflate(inflater, container, false)
        return _b1!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selectedMember = preSelectedMember
        bindStep1()
    }

    override fun onDestroyView() {
        _b1 = null; _b2 = null; _b3 = null
        super.onDestroyView()
    }

    // ══════════════════════════════════════════════════════════════════════
    // STEP 1 — Select Borrower
    // ══════════════════════════════════════════════════════════════════════
    private fun bindStep1() {
        val b = _b1 ?: return
        b.btnClose.setOnClickListener { dismiss() }

        val adapter = MemberSelectAdapter(selectedMember) { member ->
            selectedMember = member
            b.btnContinue.isEnabled = true
            b.btnContinue.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#1A56A0"))
            b.btnContinue.setTextColor(android.graphics.Color.WHITE)
        }
        b.rvMembers.layoutManager = LinearLayoutManager(requireContext())
        b.rvMembers.adapter = adapter
        adapter.submitList(members)

        // If preselected, enable button immediately
        if (selectedMember != null) {
            b.btnContinue.isEnabled = true
            b.btnContinue.backgroundTintList =
                android.content.res.ColorStateList.valueOf(
                    android.graphics.Color.parseColor("#1A56A0"))
            b.btnContinue.setTextColor(android.graphics.Color.WHITE)
        }

        b.etSearchMember.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, st: Int, bef: Int, c: Int) {
                val q = s?.toString().orEmpty()
                b.btnClearSearch.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
                adapter.submitList(members.filter {
                    it.name.contains(q, ignoreCase = true) ||
                            it.phone.contains(q, ignoreCase = true)
                })
            }
        })
        b.btnClearSearch.setOnClickListener { b.etSearchMember.setText("") }
        b.btnContinue.setOnClickListener {
            val m = selectedMember ?: return@setOnClickListener
            swapToStep2(m)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // STEP 2 — Loan Details
    // ══════════════════════════════════════════════════════════════════════
    private fun swapToStep2(member: Member) {
        _b2 = DialogLoanStep2Binding.inflate(LayoutInflater.from(requireContext()))
        swapContent(_b2!!.root)
        bindStep2(member)
    }

    private fun bindStep2(member: Member) {
        val b   = _b2 ?: return
        val ctx = requireContext()
        val dp  = ctx.resources.displayMetrics.density

        b.btnClose.setOnClickListener { dismiss() }
        b.tvMemberAvatar.text = member.avatar
        b.tvMemberName.text   = member.name
        b.tvExistingLoan.text = if (member.amtBorrowed > 0)
            "Current loan: ${CurrencyUtil.format(member.amtBorrowed)}"
        else "No existing loan"

        b.btnChangeMember.setOnClickListener {
            _b2 = null
            swapContent(_b1!!.root)
            bindStep1()
        }

        // Quick-fill amounts
        addChips(b.layoutAmountQuickfill, QUICK_AMOUNTS.map { CurrencyUtil.format(it) to it.toString() },
            b.etLoanAmount, "#1A56A0", dp)

        // Quick-fill rates
        addChips(b.layoutRateQuickfill, QUICK_RATES.map { "${it}%" to it.toString() },
            b.etLoanRate, "#1F7A4A", dp)

        // Purpose grid — row 1: 3 items, row 2: 3 items
        var selectedPurpose = ""
        val allPurposeViews = mutableListOf<TextView>()

        PURPOSES.forEachIndexed { index, purpose ->
            val chip = TextView(ctx).apply {
                text     = purpose; textSize = 12f; gravity = Gravity.CENTER
                setTextColor(Color.parseColor("#4A5550"))
                background = ContextCompat.getDrawable(ctx, R.drawable.bg_purpose_chip)
                setPadding((12*dp).toInt(), (9*dp).toInt(), (12*dp).toInt(), (9*dp).toInt())
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    .also { it.marginEnd = if (index % 3 != 2) (8*dp).toInt() else 0 }
                setOnClickListener {
                    selectedPurpose = purpose
                    allPurposeViews.forEach { v ->
                        v.background = ContextCompat.getDrawable(ctx, R.drawable.bg_purpose_chip)
                        v.setTextColor(Color.parseColor("#4A5550"))
                    }
                    background = ContextCompat.getDrawable(ctx, R.drawable.bg_purpose_chip_selected)
                    setTextColor(Color.parseColor("#1A56A0"))
                    setTypeface(null, Typeface.BOLD)
                }
            }
            allPurposeViews.add(chip)
            if (index < 3) b.layoutPurposeRow1.addView(chip)
            else b.layoutPurposeRow2.addView(chip)
        }

        // Live interest preview — updates when amount/rate changes
        fun refreshPreview() {
            val amt  = b.etLoanAmount.text.toString().toLongOrNull() ?: 0L
            val rate = b.etLoanRate.text.toString().toFloatOrNull() ?: 2f
            if (amt > 0) {
                val monthly     = (amt * rate / 100).toLong()
                val outstanding = member.amtBorrowed + amt
                b.cardInterestPreview.visibility = View.VISIBLE
                b.layoutPreviewRows.removeAllViews()
                listOf(
                    "Loan Amount"       to CurrencyUtil.format(amt),
                    "Rate"              to "$rate% / month",
                    "Monthly Interest"  to CurrencyUtil.format(monthly),
                    "Total Outstanding" to CurrencyUtil.format(outstanding)
                ).forEach { (label, value) ->
                    b.layoutPreviewRows.addView(LinearLayout(ctx).apply {
                        orientation = LinearLayout.HORIZONTAL
                        gravity     = Gravity.CENTER_VERTICAL
                        setPadding(0, (6*dp).toInt(), 0, (6*dp).toInt())
                        addView(TextView(ctx).apply {
                            text = label; textSize = 12f
                            setTextColor(Color.parseColor("#4A5550"))
                            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                        })
                        addView(TextView(ctx).apply {
                            text = value; textSize = 13f; setTypeface(null, Typeface.BOLD)
                            setTextColor(Color.parseColor("#1A56A0"))
                        })
                    })
                }
            } else {
                b.cardInterestPreview.visibility = View.GONE
            }
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, st: Int, bef: Int, c: Int) { refreshPreview() }
        }
        b.etLoanAmount.addTextChangedListener(watcher)
        b.etLoanRate.addTextChangedListener(watcher)

        b.btnBack.setOnClickListener {
            _b2 = null; swapContent(_b1!!.root); bindStep1()
        }
        b.btnReview.setOnClickListener {
            val amt  = b.etLoanAmount.text.toString().toLongOrNull() ?: 0L
            val rate = b.etLoanRate.text.toString().toFloatOrNull() ?: 2f
            if (amt <= 0) {
                Toast.makeText(ctx, "Enter a valid loan amount.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            swapToStep3(member, amt, rate, selectedPurpose)
        }
    }

    private fun addChips(
        container: LinearLayout,
        items: List<Pair<String, String>>,
        targetEt: EditText,
        hexColor: String,
        dp: Float
    ) {
        container.removeAllViews()
        val ctx   = requireContext()
        val color = Color.parseColor(hexColor)
        items.forEach { (label, value) ->
            val chip = TextView(ctx).apply {
                text     = label; textSize = 11f; gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD); setTextColor(color)
                background = ContextCompat.getDrawable(ctx, R.drawable.bg_quickfill_btn)
                setPadding((12*dp).toInt(), (7*dp).toInt(), (12*dp).toInt(), (7*dp).toInt())
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).also { it.marginEnd = (8*dp).toInt() }
                setOnClickListener {
                    targetEt.setText(value)
                    targetEt.setSelection(targetEt.text.length)
                    for (i in 0 until container.childCount) {
                        val v = container.getChildAt(i) as? TextView ?: continue
                        v.background = ContextCompat.getDrawable(ctx, R.drawable.bg_quickfill_btn)
                        v.setTextColor(color)
                    }
                    background = ContextCompat.getDrawable(ctx, R.drawable.bg_quickfill_btn_selected)
                    setTextColor(Color.WHITE)
                }
            }
            container.addView(chip)
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // STEP 3 — Review & Issue
    // ══════════════════════════════════════════════════════════════════════
    private fun swapToStep3(member: Member, amount: Long, rate: Float, purpose: String) {
        _b3 = DialogLoanStep3Binding.inflate(LayoutInflater.from(requireContext()))
        swapContent(_b3!!.root)
        bindStep3(member, amount, rate, purpose)
    }

    private fun bindStep3(member: Member, amount: Long, rate: Float, purpose: String) {
        val b   = _b3 ?: return
        val ctx = requireContext()
        val dp  = ctx.resources.displayMetrics.density

        b.btnClose.setOnClickListener { dismiss() }
        b.tvMemberAvatar.text    = member.avatar
        b.tvMemberName.text      = member.name
        b.tvLoanDate.text        = "Issued ${SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date())}"
        b.tvLoanAmountLarge.text = CurrencyUtil.format(amount)

        val monthly     = (amount * rate / 100).toLong()
        val outstanding = member.amtBorrowed + amount

        b.layoutSummary.removeAllViews()
        val rows = buildList {
            add(Triple("💸 Loan Amount",    CurrencyUtil.format(amount),      "#1A56A0"))
            add(Triple("📈 Interest Rate",  "$rate% per month",               "#1F7A4A"))
            add(Triple("💰 Monthly Interest", CurrencyUtil.format(monthly),   "#1F7A4A"))
            if (purpose.isNotBlank()) add(Triple("📋 Purpose", purpose,       "#4A5550"))
            if (member.amtBorrowed > 0) {
                add(Triple("📊 Previous Loan", CurrencyUtil.format(member.amtBorrowed), "#D4600A"))
                add(Triple("📊 Total Outstanding", CurrencyUtil.format(outstanding),    "#D4600A"))
            }
        }

        rows.forEachIndexed { i, (label, value, color) ->
            if (i > 0) {
                b.layoutSummary.addView(View(ctx).apply {
                    setBackgroundColor(ContextCompat.getColor(ctx, R.color.divider_color))
                    layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
                })
            }
            b.layoutSummary.addView(LinearLayout(ctx).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity     = Gravity.CENTER_VERTICAL
                setPadding((16*dp).toInt(), (13*dp).toInt(), (16*dp).toInt(), (13*dp).toInt())
                addView(TextView(ctx).apply {
                    text     = label; textSize = 13f
                    setTextColor(ContextCompat.getColor(ctx, R.color.text_muted))
                    layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                })
                addView(TextView(ctx).apply {
                    text     = value; textSize = 14f; setTypeface(null, Typeface.BOLD)
                    setTextColor(Color.parseColor(color))
                })
            })
        }

        b.tvWarning.text = "Monthly interest of ${CurrencyUtil.format(monthly)} will be due every month." +
                " Missing payments will trigger a penalty."

        b.btnEdit.setOnClickListener { swapToStep2(member) }
        b.btnIssue.setOnClickListener {
            onLoanIssued?.invoke(member.id, amount, rate, purpose)
            dismiss()
        }
    }

    // ── Swap sheet content ─────────────────────────────────────────────────────
    private fun swapContent(newView: View) {
        val decorView   = dialog?.window?.decorView as? ViewGroup ?: return
        val content     = decorView.findViewById<ViewGroup>(android.R.id.content) ?: return
        val coordinator = content.getChildAt(0) as? ViewGroup ?: return
        val bottomSheet = findBottomSheet(coordinator) ?: return
        bottomSheet.removeAllViews()
        bottomSheet.addView(newView)
    }

    private fun findBottomSheet(vg: ViewGroup): ViewGroup? {
        for (i in 0 until vg.childCount) {
            val child = vg.getChildAt(i)
            if (child is ViewGroup) {
                if (child.id == com.google.android.material.R.id.design_bottom_sheet) return child
                findBottomSheet(child)?.let { return it }
            }
        }
        return null
    }

    // ── Member select adapter ──────────────────────────────────────────────────
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
        override fun onBindViewHolder(holder: VH, pos: Int) = holder.bind(getItem(pos))

        inner class VH(private val b: ItemMemberStep1Binding) :
            RecyclerView.ViewHolder(b.root) {
            fun bind(m: Member) {
                val ctx      = itemView.context
                val selected = currentSel?.id == m.id
                b.tvAvatar.text   = m.avatar
                b.tvName.text     = m.name
                b.tvLoanInfo.text = if (m.amtBorrowed > 0)
                    "Existing loan: ${CurrencyUtil.format(m.amtBorrowed)}"
                else "No active loan — eligible for new loan ✓"
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