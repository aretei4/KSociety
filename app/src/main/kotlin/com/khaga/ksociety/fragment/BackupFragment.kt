package com.khaga.ksociety.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.khaga.ksociety.R
import com.khaga.ksociety.activity.MainActivity
import com.khaga.ksociety.databinding.FragmentBackupBinding
import com.khaga.ksociety.viewmodel.BackupViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupFragment : Fragment() {

    private var _binding: FragmentBackupBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: BackupViewModel

    companion object {
        private const val PREFS_NAME = "bc_backup_prefs"
        private const val KEY_LAST   = "last_backup_time"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[BackupViewModel::class.java]

        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }
        binding.etApiUrl.setText(viewModel.getCurrentUrl(requireContext()))

        setupClickListeners()
        setupObservers()

        viewModel.refreshStats()
        showLastBackupTime()
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).hideBottomNav()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as MainActivity).showBottomNav()
    }

    private fun setupClickListeners() {
        binding.btnSaveUrl.setOnClickListener {
            val url = binding.etApiUrl.text.toString().trim()
            if (url.isBlank() || !url.startsWith("http")) {
                Toast.makeText(
                    requireContext(),
                    "Enter a valid URL starting with http",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.saveBaseUrl(requireContext(), url)
            Toast.makeText(requireContext(), "\u2713 API endpoint saved", Toast.LENGTH_SHORT).show()
        }
        binding.btnBackup.setOnClickListener  { viewModel.performBackup(requireContext()) }
        binding.btnRestore.setOnClickListener { viewModel.performRestore(requireContext()) }
        binding.btnClearData.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Clear All Data?")
                .setMessage("This will permanently delete all funds, members, and payments. This cannot be undone.")
                .setPositiveButton("Clear All") { _, _ ->
                    viewModel.clearAllData {
                        _binding ?: return@clearAllData
                        Toast.makeText(requireContext(), "All data cleared.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun setupObservers() {
        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            _binding ?: return@observe
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnBackup.isEnabled    = !isLoading
            binding.btnRestore.isEnabled   = !isLoading
            binding.btnClearData.isEnabled = !isLoading
            binding.btnBackup.alpha        = if (isLoading) 0.5f else 1f
            binding.btnRestore.alpha       = if (isLoading) 0.5f else 1f
            binding.btnClearData.alpha     = if (isLoading) 0.5f else 1f
        }

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            _binding ?: return@observe
            binding.tvStatFunds.text    = stats.funds.toString()
            binding.tvStatMembers.text  = stats.members.toString()
            binding.tvStatPayments.text = stats.payments.toString()
        }

        viewModel.result.observe(viewLifecycleOwner) { result ->
            _binding ?: return@observe
            binding.layoutResult.visibility = View.VISIBLE
            binding.tvResultIcon.text = if (result.success) "\u2705" else "\u274C"
            binding.tvResultMessage.text = result.message
            binding.tvResultMessage.setTextColor(
                requireContext().getColor(
                    if (result.success) R.color.color_green else R.color.color_red
                )
            )
            if (result.success) {
                requireContext()
                    .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit().putLong(KEY_LAST, System.currentTimeMillis()).apply()
                showLastBackupTime()
            }
        }
    }

    private fun showLastBackupTime() {
        _binding ?: return
        val lastTime = requireContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_LAST, 0L)
        binding.tvLastBackup.text = if (lastTime > 0L) {
            "Last backup: ${
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(lastTime))
            }"
        } else {
            "No backup yet"
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
