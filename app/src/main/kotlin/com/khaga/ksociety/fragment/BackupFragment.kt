package com.khaga.ksociety.fragment

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
        private const val PREFS    = "ksociety_backup"
        private const val KEY_LAST = "last_backup_time"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBackupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[BackupViewModel::class.java]

        binding.btnBack.setOnClickListener { requireActivity().onBackPressed() }

        // Pre-fill saved values
        binding.etApiUrl.setText(viewModel.getCurrentUrl(requireContext()))
        binding.etDeviceId.setText(viewModel.getDeviceId(requireContext()))

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
        // Save server URL
        binding.btnSaveUrl.setOnClickListener {
            val url = binding.etApiUrl.text.toString().trim()
            if (url.isBlank() || (!url.startsWith("http://") && !url.startsWith("https://"))) {
                Toast.makeText(requireContext(), "Enter a valid URL starting with http", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveBaseUrl(requireContext(), url)
            Toast.makeText(requireContext(), "✓ Server URL saved", Toast.LENGTH_SHORT).show()
        }

        // Save device ID (IMEI or phone number)
        binding.btnSaveDeviceId.setOnClickListener {
            val id = binding.etDeviceId.text.toString().trim()
            if (id.isBlank()) {
                Toast.makeText(requireContext(), "Enter IMEI or phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveDeviceId(requireContext(), id)
            Toast.makeText(requireContext(), "✓ Device ID saved", Toast.LENGTH_SHORT).show()
        }

        binding.btnBackup.setOnClickListener {
            val id = binding.etDeviceId.text.toString().trim()
            if (id.isBlank()) {
                Toast.makeText(requireContext(), "Set your IMEI or phone number first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveDeviceId(requireContext(), id)
            viewModel.performBackup(requireContext())
        }

        binding.btnRestore.setOnClickListener {
            val id = binding.etDeviceId.text.toString().trim()
            if (id.isBlank()) {
                Toast.makeText(requireContext(), "Set your IMEI or phone number first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.saveDeviceId(requireContext(), id)
            // Confirm before overwriting local data
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Restore from server?")
                .setMessage("This will replace ALL local data with the server backup for device: $id")
                .setPositiveButton("Restore") { _, _ -> viewModel.performRestore(requireContext()) }
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
            binding.btnBackup.alpha        = if (isLoading) 0.5f else 1f
            binding.btnRestore.alpha       = if (isLoading) 0.5f else 1f
        }

        viewModel.stats.observe(viewLifecycleOwner) { stats ->
            _binding ?: return@observe
            binding.tvStatFunds.text    = stats.funds.toString()
            binding.tvStatMembers.text  = stats.members.toString()
            binding.tvStatPayments.text = stats.payments.toString()
        }

        viewModel.result.observe(viewLifecycleOwner) { result ->
            _binding ?: return@observe
            binding.layoutResult.visibility  = View.VISIBLE
            binding.tvResultIcon.text        = if (result.success) "✅" else "❌"
            binding.tvResultMessage.text     = result.message
            binding.tvResultMessage.setTextColor(
                requireContext().getColor(
                    if (result.success) R.color.color_green else R.color.color_red
                )
            )
            if (result.success) {
                requireContext()
                    .getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
                    .edit().putLong(KEY_LAST, System.currentTimeMillis()).apply()
                showLastBackupTime()
                viewModel.refreshStats()
            }
        }
    }

    private fun showLastBackupTime() {
        _binding ?: return
        val lastTime = requireContext()
            .getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
            .getLong(KEY_LAST, 0L)
        binding.tvLastBackup.text = if (lastTime > 0L)
            "Last backup: ${SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(lastTime))}"
        else "No backup yet"
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}