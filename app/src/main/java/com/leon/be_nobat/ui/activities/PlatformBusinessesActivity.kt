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
import com.leon.be_nobat.domain.model.ManagementBusiness
import com.leon.be_nobat.helpers.BaseActivity
import com.leon.be_nobat.ui.adapters.BaseAdminAdapter
import com.leon.be_nobat.ui.adapters.simpleDiff
import com.leon.be_nobat.ui.adapters.AdminViewHolder
import com.leon.be_nobat.ui.view_models.platform.PlatformBusinessesUiState
import com.leon.be_nobat.ui.view_models.platform.PlatformBusinessesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlatformBusinessesActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_admin_list

    private val viewModel: PlatformBusinessesViewModel by viewModel()
    private lateinit var adapter: BusinessesAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<TabLayout>(R.id.statusTabs).visibility = View.GONE
    }

    override fun setupViews() {
        setToolbarTitle(getString(R.string.platform_businesses))
        setMenuVisibility(false)

        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { viewModel.load() }

        adapter = BusinessesAdapter(
            onToggle = { biz -> viewModel.toggleActive(biz.id, !biz.active) },
            onDelete = { biz -> viewModel.delete(biz.id) },
        )
        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(this@PlatformBusinessesActivity)
            this.adapter = this@PlatformBusinessesActivity.adapter
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun render(state: PlatformBusinessesUiState) {
        swipeRefresh.isRefreshing = state.loading
        findViewById<android.widget.ProgressBar>(R.id.loadingProgress).isVisible = state.loading && state.items.isEmpty()
        findViewById<android.widget.TextView>(R.id.emptyStateText).isVisible = !state.loading && state.items.isEmpty()
        adapter.submitList(state.items)
        state.error?.let { Snackbar.make(swipeRefresh, it, Snackbar.LENGTH_SHORT).show() }
    }

    private class BusinessesAdapter(
        private val onToggle: (ManagementBusiness) -> Unit,
        private val onDelete: (ManagementBusiness) -> Unit,
    ) : BaseAdminAdapter<ManagementBusiness>(simpleDiff({ a, b -> a.id == b.id }, { a, b -> a == b })) {
        override fun bindHolder(holder: AdminViewHolder, item: ManagementBusiness) {
            holder.binding.apply {
                tvTitle.text = item.name
                tvSubtitle.text = "${item.category} • ${item.city}"
                tvBody.text = item.description
                tvStatus.text = if (item.active) root.context.getString(R.string.set_active) else root.context.getString(R.string.set_inactive)
                btnAction1.text = if (item.active) root.context.getString(R.string.set_inactive) else root.context.getString(R.string.set_active)
                btnAction2.isVisible = false
                btnAction1.setOnClickListener { onToggle(item) }
            }
        }
    }
}
