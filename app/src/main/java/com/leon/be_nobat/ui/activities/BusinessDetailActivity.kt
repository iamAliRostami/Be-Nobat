package com.leon.be_nobat.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.leon.be_nobat.R
import com.leon.be_nobat.helpers.BaseActivity
import com.leon.be_nobat.ui.view_models.BusinessDetailUiState
import com.leon.be_nobat.ui.view_models.BusinessDetailViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class BusinessDetailActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_business_detail

    private val viewModel: BusinessDetailViewModel by viewModel()

    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun setupViews() {
        setToolbarTitle(getString(R.string.book_now))
        setMenuVisibility(false)

        swipeRefresh = findViewById(R.id.swipeRefresh)
        swipeRefresh.setOnRefreshListener { viewModel.load() }

        val businessId = intent.getStringExtra("businessId") ?: ""
        viewModel.setBusinessId(businessId)

        findViewById<android.widget.ImageButton>(R.id.btnFavorite).setOnClickListener {
            viewModel.toggleFavorite()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnBook).setOnClickListener {
            val id = viewModel.state.value.business?.id ?: return@setOnClickListener
            val intent = Intent(this, BookingActivity::class.java).apply {
                putExtra("businessId", id)
            }
            startActivity(intent)
        }
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun render(state: BusinessDetailUiState) {
        swipeRefresh.isRefreshing = state.loading
        findViewById<android.widget.ProgressBar>(R.id.loadingProgress).isVisible = state.loading && state.business == null
        findViewById<android.widget.TextView>(R.id.errorText).apply {
            isVisible = state.error != null
            text = state.error
        }
        val b = state.business ?: return
        findViewById<android.widget.TextView>(R.id.tvName).text = b.name
        findViewById<android.widget.TextView>(R.id.tvCategoryCity).text = "${b.category} • ${b.city}"
        findViewById<android.widget.TextView>(R.id.tvDescription).text = b.description
        findViewById<android.widget.TextView>(R.id.tvStats).text = "${b.branchCount} شعب • ${b.serviceCount} خدمات • ★ ${b.rating ?: "—"} (${b.reviewCount} نظر)"
        findViewById<android.widget.TextView>(R.id.tvBranches).text = b.branches.joinToString("\n\n") { "• ${it.name} — ${it.address}" }
        findViewById<android.widget.TextView>(R.id.tvServices).text = b.services.joinToString("\n\n") {
            "• ${it.name} (${it.durationMinutes} دقیقه) — ${it.price.toInt()} ${it.currency}"
        }
        findViewById<android.widget.TextView>(R.id.tvReviews).text = b.reviews.joinToString("\n\n") {
            "★ ${it.rating} — ${it.customerName}\n${it.comment}"
        }
        findViewById<android.widget.ImageButton>(R.id.btnFavorite).setColorFilter(if (state.isFavorite) getColor(R.color.error_red) else getColor(R.color.brand_accent))
    }
}
