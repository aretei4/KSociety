package com.khaga.ksociety.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.khaga.ksociety.databinding.FragmentAddMemberBinding
import com.khaga.ksociety.model.Member
import com.khaga.ksociety.viewmodel.FundDetailViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddMemberFragment : Fragment() {

    private var _binding: FragmentAddMemberBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: FundDetailViewModel
    private var fundId = -1L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddMemberBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fundId    = arguments?.getLong("fund_id") ?: -1L
        // Use requireActivity() scope so ViewModel survives back navigation
        viewModel = ViewModelProvider(requireActivity())[FundDetailViewModel::class.java]

        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }
        binding.btnSaveMember.isEnabled = false
        binding.btnSaveMember.alpha     = 0.5f

        observeFund()
        setupTextWatchers()
        binding.btnSaveMember.setOnClickListener { saveMember() }
    }

    /** Pre-fill contribution/fee from the fund's defaults */
    private fun observeFund() {
        viewModel.fund.observe(viewLifecycleOwner) { fund ->
            _binding ?: return@observe
            fund ?: return@observe
            binding.tvFundName.text = "to ${fund.name}"
            if (fund.monthlyAmount > 0 && binding.etContribution.text.isBlank())
                binding.etContribution.setText(fund.monthlyAmount.toString())
            if (fund.memberFee > 0 && binding.etFee.text.isBlank())
                binding.etFee.setText(fund.memberFee.toString())
        }
    }

    private fun setupTextWatchers() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                _binding ?: return
                updatePreview()
                validateForm()
            }
        }
        binding.etName.addTextChangedListener(watcher)
        binding.etContribution.addTextChangedListener(watcher)
    }

    private fun updatePreview() {
        val name = binding.etName.text.toString().trim()
        if (name.isNotEmpty()) {
            val initials = Member.generateAvatar(name)
            binding.frameAvatarPreview.visibility = View.VISIBLE
            binding.tvAvatarPreview.text  = initials
            binding.layoutPreview.visibility = View.VISIBLE
            binding.tvPreviewName.text    = name
            binding.tvPreviewAvatar.text  = initials

            val contrib = binding.etContribution.text.toString().toLongOrNull() ?: 0L
            val fee     = binding.etFee.text.toString().toLongOrNull() ?: 0L
            val loan    = binding.etLoan.text.toString().toLongOrNull() ?: 0L
            binding.tvPreviewDetail.text = buildString {
                append("\u20b9$contrib/mo")
                if (fee  > 0) append(" \u00b7 Fee \u20b9$fee")
                if (loan > 0) append(" \u00b7 Loan \u20b9$loan")
            }
        } else {
            binding.frameAvatarPreview.visibility = View.GONE
            binding.layoutPreview.visibility      = View.GONE
        }
    }

    private fun validateForm() {
        val valid = binding.etName.text.isNotBlank() &&
                binding.etContribution.text.isNotBlank()
        binding.btnSaveMember.isEnabled = valid
        binding.btnSaveMember.alpha     = if (valid) 1f else 0.5f
    }

    private fun saveMember() {
        val name = binding.etName.text.toString().trim()
        val member = Member(
            name         = name,
            phone        = binding.etPhone.text.toString().trim(),
            avatar       = Member.generateAvatar(name),
            contribution = binding.etContribution.text.toString().toLongOrNull() ?: 0L,
            fees         = binding.etFee.text.toString().toLongOrNull() ?: 0L,
            amtBorrowed  = binding.etLoan.text.toString().toLongOrNull() ?: 0L,
            joinDate     = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date()),
            fundId       = fundId
        )

        viewModel.insertMember(member) { success ->
            activity?.runOnUiThread {
                if (success) {
                    Toast.makeText(requireContext(), "\u2705 $name added!", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressed()
                } else {
                    Toast.makeText(requireContext(), "Failed to add member.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
