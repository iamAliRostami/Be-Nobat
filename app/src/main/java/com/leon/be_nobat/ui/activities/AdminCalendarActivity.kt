package com.leon.be_nobat.ui.activities

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.leon.be_nobat.R
import com.leon.be_nobat.databinding.ItemAdminAppointmentBinding
import com.leon.be_nobat.domain.model.ManagementAppointment
import com.leon.be_nobat.helpers.BaseActivity
import com.leon.be_nobat.ui.adapters.BaseAdminAdapter
import com.leon.be_nobat.ui.adapters.simpleDiff
import com.leon.be_nobat.ui.adapters.AdminViewHolder
import com.leon.be_nobat.ui.view_models.admin.AdminCalendarUiState
import com.leon.be_nobat.ui.view_models.admin.AdminCalendarViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdminCalendarActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_admin_list

    private val viewModel: AdminCalendarViewModel by viewModel()
    private lateinit var adapter: ApptsAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<TabLayout>(R.id.statusTabs)
    }

    override fun setupViews() {
        setToolbarTitle(getString(R.string.admin_calendar))
        setMenuVisibility(false)

        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { viewModel.load() }

        adapter = ApptsAdapter(
            onConfirm = { appt -> viewModel.changeStatus(appt.id, "Confirmed") },
            onComplete = { appt -> viewModel.changeStatus(appt.id, "Completed") },
            onNoShow = { appt -> viewModel.changeStatus(appt.id, "NoShow") },
            onCancel = { appt -> viewModel.changeStatus(appt.id, "Cancelled") },
        )
        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(this@AdminCalendarActivity)
            this.adapter = this@AdminCalendarActivity.adapter
        }

        val tabs = findViewById<TabLayout>(R.id.statusTabs)
        listOf("" to "همه", "Pending" to getString(R.string.appointment_status_pending),
            "Confirmed" to getString(R.string.appointment_status_confirmed),
            "Completed" to getString(R.string.appointment_status_completed),
            "Cancelled" to getString(R.string.appointment_status_cancelled),
            "NoShow" to getString(R.string.appointment_status_no_show)
        ).forEach { (key, label) ->
            tabs.addTab(tabs.newTab().setText(label).also { it.tag = key })
        }
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewModel.setStatusFilter((tab.tag as? String)?.ifBlank { null })
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun render(state: AdminCalendarUiState) {
        swipeRefresh.isRefreshing = state.loading
        findViewById<android.widget.ProgressBar>(R.id.loadingProgress).isVisible = state.loading && state.items.isEmpty()
        findViewById<android.widget.TextView>(R.id.emptyStateText).isVisible = !state.loading && state.items.isEmpty()
        adapter.submitList(state.items)
    }

    private class ApptsAdapter(
        private val onConfirm: (ManagementAppointment) -> Unit,
        private val onComplete: (ManagementAppointment) -> Unit,
        private val onNoShow: (ManagementAppointment) -> Unit,
        private val onCancel: (ManagementAppointment) -> Unit,
    ) : BaseAdminAdapter<ManagementAppointment>(
        simpleDiff({ a, b -> a.id == b.id }, { a, b -> a == b })
    ) {
        override fun bindHolder(holder: AdminViewHolder, item: ManagementAppointment) {
            holder.binding.apply {
                tvTitle.text = item.businessName
                tvSubtitle.text = "${item.branchName} • ${item.customerName} • ${item.serviceNames.joinToString("، ")}"
                tvBody.text = "${com.leon.be_nobat.helpers.JalaliDateHelper.formatIsoDateTime(item.startsAt)} • کد: ${com.leon.be_nobat.helpers.PersianUtils.toPersianDigits(item.trackingCode)} • ${com.leon.be_nobat.helpers.PersianUtils.formatPrice(item.finalPrice, item.currency)}"
                tvStatus.text = item.status
                btnAction1.text = root.context.getString(R.string.complete_appointment)
                btnAction2.text = root.context.getString(R.string.cancel_appointment_admin)
                btnAction1.isVisible = item.status == "Pending" || item.status == "Confirmed"
                btnAction2.isVisible = item.status == "Pending" || item.status == "Confirmed"
                btnAction1.setOnClickListener {
                    val next = if (item.status == "Pending") onConfirm else onComplete
                    next(item)
                }
                btnAction2.setOnClickListener { onCancel(item) }
            }
        }
    }
}
