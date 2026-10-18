package com.leon.be_nobat.ui.activities

import android.content.Intent
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.navigation.NavigationView
import com.leon.be_nobat.R
import com.leon.be_nobat.helpers.BaseActivity
import com.leon.be_nobat.ui.view_models.admin.AdminDashboardUiState
import com.leon.be_nobat.ui.view_models.admin.AdminDashboardViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdminHomeActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_admin_home

    private val viewModel: AdminDashboardViewModel by viewModel()
    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun setupViews() {
        setToolbarTitle(true) // Remove the dull base toolbar

        drawerLayout = findViewById(R.id.drawerLayout)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { viewModel.load() }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnToggleDrawer).setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLanguage).apply {
            text = getCurrentLanguageLabel()
            setOnClickListener { showLanguageMenu(it) }
        }

        findViewById<android.widget.ImageButton>(R.id.btnThemeToggle).setOnClickListener {
            setupThemeToggle()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPublicView).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            finishAffinity()
        }

        val navView = findViewById<NavigationView>(R.id.navView)
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.drawer_dashboard -> { drawerLayout.closeDrawer(GravityCompat.START); true }
                R.id.drawer_calendar -> { startActivity(Intent(this, AdminCalendarActivity::class.java)); true }
                R.id.drawer_branches -> { startActivity(Intent(this, AdminBranchesActivity::class.java)); true }
                R.id.drawer_services -> { startActivity(Intent(this, AdminServicesActivity::class.java)); true }
                R.id.drawer_availability -> { startActivity(Intent(this, AdminAvailabilityActivity::class.java)); true }
                R.id.drawer_reviews -> { startActivity(Intent(this, AdminReviewsActivity::class.java)); true }
                R.id.drawer_customers -> { startActivity(Intent(this, AdminCustomersActivity::class.java)); true }
                R.id.drawer_team -> { startActivity(Intent(this, AdminTeamActivity::class.java)); true }
                R.id.drawer_platform_businesses -> { startActivity(Intent(this, PlatformBusinessesActivity::class.java)); true }
                R.id.drawer_platform_catalog -> { startActivity(Intent(this, PlatformCatalogActivity::class.java)); true }
                R.id.drawer_platform_categories -> { startActivity(Intent(this, PlatformCategoriesActivity::class.java)); true }
                R.id.drawer_platform_access -> { startActivity(Intent(this, PlatformAccessActivity::class.java)); true }
                R.id.drawer_back_to_user_view -> {
                    startActivity(Intent(this, HomeActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finishAffinity()
                    true
                }
                R.id.drawer_logout -> {
                    com.leon.be_nobat.ui.activities.auth.LogoutHelper.logoutAndRestart(this)
                    true
                }
                else -> false
            }
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCalendar).setOnClickListener {
            startActivity(Intent(this, AdminCalendarActivity::class.java))
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBranches).setOnClickListener {
            startActivity(Intent(this, AdminBranchesActivity::class.java))
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnReviews).setOnClickListener {
            startActivity(Intent(this, AdminReviewsActivity::class.java))
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun render(state: AdminDashboardUiState) {
        swipeRefresh.isRefreshing = state.loading
        findViewById<android.widget.ProgressBar>(R.id.loadingProgress).isVisible = state.loading && state.dashboard == null
        state.error?.let {
            findViewById<android.widget.TextView>(R.id.tvError).apply {
                isVisible = true
                text = it
            }
        }
        state.dashboard?.let { d ->
            findViewById<android.widget.TextView>(R.id.tvBusinessesValue).text =
                com.leon.be_nobat.helpers.PersianUtils.toPersianDigits(d.businesses)
            findViewById<android.widget.TextView>(R.id.tvBranchesValue).text =
                com.leon.be_nobat.helpers.PersianUtils.toPersianDigits(d.branches)
            findViewById<android.widget.TextView>(R.id.tvAppointmentsValue).text =
                com.leon.be_nobat.helpers.PersianUtils.toPersianDigits(d.appointments)
            findViewById<android.widget.TextView>(R.id.tvPendingValue).text =
                com.leon.be_nobat.helpers.PersianUtils.toPersianDigits(d.pending)
            findViewById<android.widget.TextView>(R.id.tvUpcomingValue).text =
                com.leon.be_nobat.helpers.PersianUtils.toPersianDigits(d.upcoming)
            findViewById<android.widget.TextView>(R.id.tvCompletedValue).text =
                com.leon.be_nobat.helpers.PersianUtils.toPersianDigits(d.completed)
            findViewById<android.widget.TextView>(R.id.tvRevenueValue).text =
                com.leon.be_nobat.helpers.PersianUtils.formatNumber(d.revenue.toLong())
            findViewById<android.widget.TextView>(R.id.tvPendingReviewsValue).text =
                com.leon.be_nobat.helpers.PersianUtils.toPersianDigits(d.pendingReviews)
        }
    }
}
