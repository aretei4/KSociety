package com.khaga.ksociety.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.text.*
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.khaga.ksociety.R
import com.khaga.ksociety.activity.MainActivity
import com.khaga.ksociety.adapter.MemberAdapter
import com.khaga.ksociety.databinding.FragmentMembersBinding
import com.khaga.ksociety.model.Member
import com.khaga.ksociety.util.CurrencyUtil
import com.khaga.ksociety.viewmodel.FundDetailViewModel

class MembersFragment : Fragment() {

    private var _binding: FragmentMembersBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FundDetailViewModel
    private lateinit var adapter: MemberAdapter
    private var fundId = -1L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMembersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fundId    = arguments?.getLong("fund_id") ?: -1L
        viewModel = ViewModelProvider(requireParentFragment())[FundDetailViewModel::class.java]

        adapter = MemberAdapter { member -> showMemberOptions(member) }
        binding.rvMembers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMembers.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {
                _binding ?: return
                binding.btnClearSearch.visibility = if ((s?.length ?: 0) > 0) View.VISIBLE else View.GONE
                viewModel.loadMembers(fundId, s.toString())
            }
        })
        binding.btnClearSearch.setOnClickListener { binding.etSearch.setText("") }
        binding.fabAddMember.setOnClickListener {
            val f = AddMemberFragment().apply {
                arguments = Bundle().apply { putLong("fund_id", fundId) }
            }
            (requireActivity() as MainActivity).navigateTo(f, "add_member")
            (requireActivity() as MainActivity).hideBottomNav()
        }
        binding.fabImportCsv.setOnClickListener {
            val sheet = ImportMembersBottomSheet.newInstance(fundId)
            sheet.onImportDone = { viewModel.loadMembers(fundId) }
            sheet.show(childFragmentManager, "import_members")
        }
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.members.observe(viewLifecycleOwner) { members ->
            _binding ?: return@observe
            adapter.submitList(members)
            binding.layoutEmpty.visibility = if (members.isEmpty()) View.VISIBLE else View.GONE
            binding.rvMembers.visibility   = if (members.isEmpty()) View.GONE   else View.VISIBLE
        }
    }

    // ── Member options ──────────────────────────────────────────────────────
    private fun showMemberOptions(member: Member) {
        val loanLabel = if (member.amtBorrowed > 0) "Give Additional Loan" else "Give Loan"
        AlertDialog.Builder(requireContext())
            .setTitle(member.name)
            .setItems(arrayOf(loanLabel, "View Details", "Delete Member")) { _, which ->
                when (which) {
                    0 -> showGiveLoanSheet(member)
                    1 -> showMemberDetail(member)
                    2 -> confirmDeleteMember(member)
                }
            }.show()
    }

    private fun showMemberDetail(member: Member) {
        val minInt = (member.amtBorrowed * 0.02).toLong()
        val msg = buildString {
            appendLine("Name:         ${member.name}")
            appendLine("Phone:        ${member.phone.ifEmpty { "—" }}")
            appendLine("Contribution: ${CurrencyUtil.format(member.contribution)}/mo")
            appendLine("Loan:         ${if (member.amtBorrowed > 0) CurrencyUtil.format(member.amtBorrowed) else "None"}")
            if (member.amtBorrowed > 0) appendLine("Min Interest: ${CurrencyUtil.format(minInt)}/mo")
            append("Joined:       ${member.joinDate}")
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Member Details")
            .setMessage(msg)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmDeleteMember(member: Member) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete ${member.name}?")
            .setMessage("This permanently deletes the member and all their payment records.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteMember(member.id, fundId) { success ->
                    activity?.runOnUiThread {
                        Toast.makeText(
                            requireContext(),
                            if (success) "${member.name} deleted." else "Failed to delete.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ══════════════════════════════════════════════════════════════════════
    private fun showGiveLoanSheet(member: Member) {
        val sheet = GiveLoanBottomSheet.newInstance()
        sheet.members          = viewModel.members.value.orEmpty()
        sheet.preSelectedMember = member
        sheet.onLoanIssued = { memberId, amount, rate, purpose ->
            val updated = member.copy(amtBorrowed = member.amtBorrowed + amount)
            viewModel.updateMember(updated) { success ->
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(),
                        if (success) "\uD83D\uDCB8 Loan of ${com.khaga.ksociety.util.CurrencyUtil.format(amount)} issued to ${member.name}!"
                        else "Failed to issue loan.",
                        Toast.LENGTH_SHORT).show()
                    if (success) viewModel.loadMembers(fundId)
                }
            }
        }
        sheet.show(childFragmentManager, "give_loan")
    }

    //  GIVE LOAN — 3-step flow (mirrors JSX GiveLoanModal exactly)
    // ══════════════════════════════════════════════════════════════════════

    /* Step 1 — confirm the member (already known, just show info + proceed) */
    private fun giveLoanStep1(member: Member) {
        val existingInfo = if (member.amtBorrowed > 0)
            "Current outstanding loan: ${CurrencyUtil.format(member.amtBorrowed)}"
        else
            "No existing loan on this member."

        AlertDialog.Builder(requireContext())
            .setTitle("Give Loan — Step 1 of 3")
            .setMessage("Borrower: ${member.name}\n\n$existingInfo\n\nTap Continue to enter the loan amount.")
            .setPositiveButton("Continue →") { _, _ -> giveLoanStep2(member) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /* Step 2 — enter amount, rate, purpose, see live interest preview */
    private fun giveLoanStep2(member: Member) {
        val ctx  = requireContext()
        val fundRate = viewModel.fund.value?.interestRate ?: 2f

        // Build the layout manually so we control every field
        val layout = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            val dp = resources.displayMetrics.density
            setPadding((16*dp).toInt(), (8*dp).toInt(), (16*dp).toInt(), (8*dp).toInt())
        }
        val dp = resources.displayMetrics.density

        fun tv(text: String, bold: Boolean = false) = TextView(ctx).apply {
            this.text = text; textSize = 12f
            if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.topMargin = (4*dp).toInt(); lp.bottomMargin = (2*dp).toInt()
            layoutParams = lp
        }

        fun et(hint: String, inputType: Int = android.text.InputType.TYPE_CLASS_NUMBER) = EditText(ctx).apply {
            this.hint = hint; this.inputType = inputType
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (48*dp).toInt())
            lp.bottomMargin = (10*dp).toInt(); layoutParams = lp
        }

        // Existing loan banner
        if (member.amtBorrowed > 0) {
            layout.addView(TextView(ctx).apply {
                text = "Existing loan: ${CurrencyUtil.format(member.amtBorrowed)}"
                textSize = 12f; setTextColor(0xFF1A56A0.toInt())
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                lp.bottomMargin = (12*dp).toInt(); layoutParams = lp
                setPadding((10*dp).toInt(), (8*dp).toInt(), (10*dp).toInt(), (8*dp).toInt())
                setBackgroundColor(0xFFEBF2FC.toInt())
            })
        }

        layout.addView(tv("Loan Amount (₹) *", bold = true))
        val etAmount = et("e.g. 10000").also { layout.addView(it) }

        layout.addView(tv("Interest Rate (% / month)", bold = true))
        val etRate = et("e.g. 2", android.text.InputType.TYPE_CLASS_NUMBER or
                android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL).also {
            it.setText(fundRate.toString()); layout.addView(it)
        }

        layout.addView(tv("Purpose (optional)"))
        val purposes = arrayOf("Personal Use", "Medical", "Business", "Education", "Home", "Other")
        var selectedPurpose = ""
        val purposeSpinner = Spinner(ctx).apply {
            adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_dropdown_item,
                arrayOf("Select purpose…") + purposes)
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, (50*dp).toInt())
            lp.bottomMargin = (10*dp).toInt(); layoutParams = lp
        }
        purposeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                selectedPurpose = if (pos > 0) purposes[pos - 1] else ""
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        layout.addView(purposeSpinner)

        // Live preview card
        val tvPreview = TextView(ctx).apply {
            textSize = 12f; setTextColor(0xFF1A56A0.toInt())
            setPadding((12*dp).toInt(), (10*dp).toInt(), (12*dp).toInt(), (10*dp).toInt())
            setBackgroundColor(0xFFEBF2FC.toInt())
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            lp.topMargin = (4*dp).toInt(); layoutParams = lp
            visibility = View.GONE
        }
        layout.addView(tvPreview)

        fun refreshPreview() {
            val amt  = etAmount.text.toString().toLongOrNull() ?: 0L
            val rate = etRate.text.toString().toFloatOrNull() ?: fundRate
            if (amt > 0) {
                val monthly     = (amt * rate / 100).toLong()
                val outstanding = member.amtBorrowed + amt
                tvPreview.text = buildString {
                    appendLine("Loan amount:       ${CurrencyUtil.format(amt)}")
                    appendLine("Rate:               $rate% / month")
                    appendLine("Monthly interest:  ${CurrencyUtil.format(monthly)}")
                    if (member.amtBorrowed > 0)
                        append("Total outstanding: ${CurrencyUtil.format(outstanding)}")
                }
                tvPreview.visibility = View.VISIBLE
            } else {
                tvPreview.visibility = View.GONE
            }
        }

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) { refreshPreview() }
        }
        etAmount.addTextChangedListener(watcher)
        etRate.addTextChangedListener(watcher)

        val sv = ScrollView(ctx).apply { addView(layout) }

        AlertDialog.Builder(ctx)
            .setTitle("Give Loan — Step 2 of 3 · ${member.name}")
            .setView(sv)
            .setPositiveButton("Review →") { _, _ ->
                val amt = etAmount.text.toString().toLongOrNull() ?: 0L
                if (amt <= 0) {
                    Toast.makeText(ctx, "Enter a valid loan amount.", Toast.LENGTH_SHORT).show()
                    giveLoanStep2(member)
                    return@setPositiveButton
                }
                val rate = etRate.text.toString().toFloatOrNull() ?: fundRate
                giveLoanStep3(member, amt, rate, selectedPurpose)
            }
            .setNegativeButton("← Back") { _, _ -> giveLoanStep1(member) }
            .show()
    }

    /* Step 3 — review summary and confirm */
    private fun giveLoanStep3(member: Member, amount: Long, rate: Float, purpose: String) {
        val monthly     = (amount * rate / 100).toLong()
        val outstanding = member.amtBorrowed + amount

        val summary = buildString {
            appendLine("Borrower:          ${member.name}")
            appendLine("─────────────────────────────")
            appendLine("Loan Amount:       ${CurrencyUtil.format(amount)}")
            appendLine("Interest Rate:     $rate% / month")
            appendLine("Monthly Interest:  ${CurrencyUtil.format(monthly)}")
            if (purpose.isNotBlank())
                appendLine("Purpose:           $purpose")
            if (member.amtBorrowed > 0) {
                appendLine("─────────────────────────────")
                appendLine("Previous Loan:     ${CurrencyUtil.format(member.amtBorrowed)}")
                appendLine("Total Outstanding: ${CurrencyUtil.format(outstanding)}")
            }
            appendLine("─────────────────────────────")
            append("⚠ Interest of ₹$monthly/mo due every month.")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Give Loan — Step 3 of 3 · Review & Confirm")
            .setMessage(summary)
            .setPositiveButton("💸 Issue Loan") { _, _ ->
                val updated = member.copy(amtBorrowed = member.amtBorrowed + amount)
                viewModel.updateMember(updated) { success ->
                    activity?.runOnUiThread {
                        if (success) {
                            Toast.makeText(
                                requireContext(),
                                "💸 Loan of ${CurrencyUtil.format(amount)} issued to ${member.name}!",
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.loadMembers(fundId)
                        } else {
                            Toast.makeText(requireContext(), "Failed to issue loan. Please try again.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNeutralButton("← Edit") { _, _ -> giveLoanStep2(member) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() { super.onResume(); viewModel.loadMembers(fundId) }
    override fun onDestroyView() { binding.rvMembers.adapter = null; super.onDestroyView(); _binding = null }
}
