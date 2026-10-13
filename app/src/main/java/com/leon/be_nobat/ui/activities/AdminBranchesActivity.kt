package com.leon.be_nobat.ui.activities

import android.os.Bundle
import android.view.View
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
import com.leon.be_nobat.domain.model.ManagementBranch
import com.leon.be_nobat.helpers.BaseActivity
import com.leon.be_nobat.ui.adapters.BaseAdminAdapter
import com.leon.be_nobat.ui.adapters.simpleDiff
import com.leon.be_nobat.ui.adapters.AdminViewHolder
import com.leon.be_nobat.ui.view_models.admin.AdminBranchesUiState
import com.leon.be_nobat.ui.view_models.admin.AdminBranchesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AdminBranchesActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_admin_list

    private val viewModel: AdminBranchesViewModel by viewModel()
    private lateinit var adapter: BranchesAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<TabLayout>(R.id.statusTabs).visibility = View.GONE
    }

    override fun setupViews() {
        setToolbarTitle(getString(R.string.admin_branches))
        setMenuVisibility(false)

        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { viewModel.load() }

        adapter = BranchesAdapter(
            onToggleActive = { branch -> viewModel.toggleActive(branch.id, !branch.active) },
            onDelete = { branch -> viewModel.delete(branch.id) },
        )
        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(this@AdminBranchesActivity)
            this.adapter = this@AdminBranchesActivity.adapter
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun render(state: AdminBranchesUiState) {
        swipeRefresh.isRefreshing = state.loading
        findViewById<android.widget.ProgressBar>(R.id.loadingProgress).isVisible = state.loading && state.items.isEmpty()
        findViewById<android.widget.TextView>(R.id.emptyStateText).isVisible = !state.loading && state.items.isEmpty()
        adapter.submitList(state.items)
        state.error?.let { Snackbar.make(swipeRefresh, it, Snackbar.LENGTH_SHORT).show() }
    }

    private class BranchesAdapter(
        private val onToggleActive: (ManagementBranch) -> Unit,
        private val onDelete: (ManagementBranch) -> Unit,
    ) : BaseAdminAdapter<ManagementBranch>(simpleDiff({ a, b -> a.id == b.id }, { a, b -> a == b })) {
        override fun bindHolder(holder: AdminViewHolder, item: ManagementBranch) {
            holder.binding.apply {
                tvTitle.text = item.name
                tvSubtitle.text = item.address
                tvBody.text = "${item.openHour}:00 - ${item.closeHour}:00"
                tvStatus.text = if (item.active) root.context.getString(R.string.set_active) else root.context.getString(R.string.set_inactive)
                btnAction1.text = if (item.active) root.context.getString(R.string.set_inactive) else root.context.getString(R.string.set_active)
                btnAction2.isVisible = false
                btnAction1.setOnClickListener { onToggleActive(item) }
            }
        }
    }
}
