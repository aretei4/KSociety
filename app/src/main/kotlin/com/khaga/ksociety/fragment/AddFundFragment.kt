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
import com.khaga.ksociety.databinding.FragmentAddFundBinding
import com.khaga.ksociety.model.Fund
import com.khaga.ksociety.viewmodel.AddFundViewModel

class AddFundFragment : Fragment() {

    private var _binding: FragmentAddFundBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: AddFundViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFundBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[AddFundViewModel::class.java]

        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }
        binding.btnCreateFund.isEnabled = false
        binding.btnCreateFund.alpha = 0.5f

        setupTextWatchers()
        setupObservers()
        binding.btnCreateFund.setOnClickListener { saveFund() }
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
        binding.etFundName.addTextChangedListener(watcher)
        binding.etMonthlyAmount.addTextChangedListener(watcher)
        binding.etNumMembers.addTextChangedListener(watcher)
    }

    private fun setupObservers() {
        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            _binding ?: return@observe
            if (success) {
                Toast.makeText(requireContext(), "\uD83C\uDF89 Fund created!", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
            } else {
                Toast.makeText(requireContext(), "Failed to create fund. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePreview() {
        val name = binding.etFundName.text.toString().trim()
        if (name.isNotEmpty()) {
            binding.layoutPreview.visibility = View.VISIBLE
            binding.tvPreviewName.text = name
            val members = binding.etNumMembers.text.toString().toIntOrNull() ?: 0
            val monthly = binding.etMonthlyAmount.text.toString().toLongOrNull() ?: 0L
            binding.tvPreviewMeta.text = "$members members \u00b7 \u20b9$monthly/mo"
        } else {
            binding.layoutPreview.visibility = View.GONE
        }
    }

    private fun validateForm() {
        val valid = binding.etFundName.text.isNotBlank() &&
                binding.etMonthlyAmount.text.isNotBlank() &&
                binding.etNumMembers.text.isNotBlank()
        binding.btnCreateFund.isEnabled = valid
        binding.btnCreateFund.alpha = if (valid) 1f else 0.5f
    }

    private fun saveFund() {
        val fund = Fund(
            name          = binding.etFundName.text.toString().trim(),
            monthlyAmount = binding.etMonthlyAmount.text.toString().toLongOrNull() ?: 0L,
            totalMembers  = binding.etNumMembers.text.toString().toIntOrNull() ?: 0,
            interestRate  = binding.etInterestRate.text.toString().toFloatOrNull() ?: 2f,
            memberFee     = binding.etMemberFee.text.toString().toLongOrNull() ?: 0L,
            status        = "allpaid",
            dotColor      = "#1F5C3A"
        )
        viewModel.saveFund(fund)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
