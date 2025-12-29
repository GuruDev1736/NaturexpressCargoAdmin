package com.naturexpresscargo.admin

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.naturexpresscargo.admin.databinding.ActivityAdminDashboardBinding
import com.naturexpresscargo.admin.fragments.AdminHomeFragment
import com.naturexpresscargo.admin.fragments.AdminProfileFragment
import com.naturexpresscargo.admin.fragments.AdminRequestsFragment
import com.naturexpresscargo.admin.fragments.AdminServicesFragment

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Handle window insets for bottom navigation
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Set up toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "Admin Dashboard"

        // Load default fragment
        if (savedInstanceState == null) {
            loadFragment(AdminHomeFragment())
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        binding.bottomBar.setOnItemSelectedListener { position ->
            when (position) {
                0 -> {
                    loadFragment(AdminHomeFragment())
                    supportActionBar?.title = "Admin Dashboard"
                }

                1 -> {
                    loadFragment(AdminServicesFragment())
                    supportActionBar?.title = "Manage Services"
                }

                2 -> {
                    loadFragment(AdminRequestsFragment())
                    supportActionBar?.title = "Manage Requests"
                }

                3 -> {
                    loadFragment(AdminProfileFragment())
                    supportActionBar?.title = "Profile"
                }
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)

        // Style the logout menu item
        menu?.findItem(R.id.action_logout)?.let { logoutItem ->
            val spannableString = android.text.SpannableString(logoutItem.title)
            spannableString.setSpan(
                android.text.style.ForegroundColorSpan(android.graphics.Color.BLACK),
                0,
                spannableString.length,
                0
            )
            logoutItem.title = spannableString
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_website -> {
                openWebPageWithCustomTab("https://naturexpresscargo.com")
                true
            }

            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun openWebPageWithCustomTab(url: String) {
        try {
            // Define color scheme with app branding
            val colorSchemeParams = CustomTabColorSchemeParams.Builder()
                .setToolbarColor(ContextCompat.getColor(this, R.color.primary_green))
                .setSecondaryToolbarColor(
                    ContextCompat.getColor(
                        this,
                        R.color.primary_green_dark
                    )
                )
                .setNavigationBarColor(
                    ContextCompat.getColor(
                        this,
                        R.color.primary_green
                    )
                )
                .build()

            // Build Custom Tabs Intent (TWA) with app branding
            val customTabsIntent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(colorSchemeParams)
                .setShowTitle(true)
                .setUrlBarHidingEnabled(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
                .setStartAnimations(
                    this,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                .setExitAnimations(
                    this,
                    android.R.anim.slide_in_left,
                    android.R.anim.slide_out_right
                )
                .build()

            // Launch the Custom Tab (TWA)
            customTabsIntent.launchUrl(this, url.toUri())
        } catch (_: Exception) {
            // Fallback to browser if Custom Tabs not available
            val browserIntent = Intent(Intent.ACTION_VIEW, url.toUri())
            startActivity(browserIntent)
        }
    }
}

