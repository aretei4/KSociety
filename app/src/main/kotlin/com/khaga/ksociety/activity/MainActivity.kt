package com.khaga.ksociety.activity

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.khaga.ksociety.R
import com.khaga.ksociety.databinding.ActivityMainBinding
import com.khaga.ksociety.fragment.*
import com.khaga.ksociety.viewmodel.HomeViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var homeViewModel: HomeViewModel

    companion object {
        const val TAG_HOME     = "home"
        const val TAG_MEMBERS  = "members_global"
        const val TAG_PAYMENTS = "payments_global"
        const val TAG_REPORTS  = "reports_global"
        const val TAG_BACKUP   = "backup"

        private const val PREFS      = "ksociety_prefs"
        private const val KEY_FIRST  = "first_launch"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        // ── Show "Clear database" dialog ONCE on very first install ──
        val prefs     = getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val isFirst   = prefs.getBoolean(KEY_FIRST, true)
        if (isFirst) {
            prefs.edit().putBoolean(KEY_FIRST, false).apply()
            showFirstLaunchDialog()
        }

        // BottomNav starts hidden — shown only inside a fund
        binding.bottomNavigation.visibility = View.GONE

        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), TAG_HOME, addToBackStack = false)
        }

        setupBottomNavigation()
    }

    private fun showFirstLaunchDialog() {
        AlertDialog.Builder(this)
            .setTitle("Welcome to KSociety!")
            .setMessage("Would you like to load sample fund data to explore the app, or start fresh?")
            .setPositiveButton("Load Sample Data") { _, _ ->
                homeViewModel.seedDatabase()
                Toast.makeText(this, "Sample data loaded ✓", Toast.LENGTH_SHORT).show()
                refreshHomeFragment()
            }
            .setNegativeButton("Start Fresh", null)
            .setCancelable(false)
            .show()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    repeat(supportFragmentManager.backStackEntryCount) {
                        supportFragmentManager.popBackStackImmediate()
                    }
                    hideBottomNav()
                    true
                }
                R.id.nav_members  -> { loadFundTab(1); true }
                R.id.nav_payments -> { loadFundTab(2); true }
                R.id.nav_reports  -> { loadFundTab(3); true }
                R.id.nav_backup   -> { navigateTo(BackupFragment(), TAG_BACKUP); true }
                else -> false
            }
        }
    }

    private fun loadFundTab(tabIndex: Int) {
        (supportFragmentManager.findFragmentByTag("fund_detail") as? FundDetailFragment)
            ?.selectTab(tabIndex)
    }

    fun loadFragment(fragment: Fragment, tag: String, addToBackStack: Boolean) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .apply { if (addToBackStack) addToBackStack(tag) }
            .commit()
    }

    fun navigateTo(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }

    fun showBottomNav() { binding.bottomNavigation.visibility = View.VISIBLE }
    fun hideBottomNav() { binding.bottomNavigation.visibility = View.GONE }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            if (supportFragmentManager.backStackEntryCount <= 1) hideBottomNav()
        } else {
            super.onBackPressed()
        }
    }

    private fun refreshHomeFragment() {
        (supportFragmentManager.findFragmentByTag(TAG_HOME) as? HomeFragment)?.refresh()
    }
}
