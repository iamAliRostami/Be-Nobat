package com.leon.be_nobat.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText
import com.leon.be_nobat.R
import com.leon.be_nobat.helpers.BaseActivity
import com.leon.be_nobat.ui.adapters.BusinessAdapter
import com.leon.be_nobat.ui.view_models.HomeUiState
import com.leon.be_nobat.ui.view_models.HomeViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_home

    private val viewModel: HomeViewModel by viewModel()

    private lateinit var adapter: BusinessAdapter
    private lateinit var businessList: RecyclerView
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private lateinit var loadingProgress: android.widget.ProgressBar
    private lateinit var emptyStateText: android.widget.TextView
    private lateinit var errorText: android.widget.TextView
    private lateinit var searchInput: TextInputEditText

    override fun setupViews() {
        setToolbarTitle(getString(R.string.app_name))
        setMenuVisibility(true)

        businessList = findViewById(R.id.businessList)
        swipeRefresh = findViewById(R.id.swipeRefresh)
        loadingProgress = findViewById(R.id.loadingProgress)
        emptyStateText = findViewById(R.id.emptyStateText)
        errorText = findViewById(R.id.errorText)
        searchInput = findViewById(R.id.searchInput)

        adapter = BusinessAdapter(
            onClick = { biz ->
                val intent = Intent(this, BusinessDetailActivity::class.java).apply {
                    putExtra("businessId", biz.id)
                }
                startActivity(intent)
            },
            onFavoriteToggle = { biz, isFav -> viewModel.toggleFavoriteFor(biz.id, isFav) },
            isFavorite = { id -> viewModel.state.value.favoriteSnapshot[id] ?: false },
        )

        businessList.layoutManager = LinearLayoutManager(this)
        businessList.adapter = adapter

        businessList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                val lm = rv.layoutManager as LinearLayoutManager
                val last = lm.findLastVisibleItemPosition()
                val total = lm.itemCount
                if (last >= total - 3 && viewModel.state.value.hasMore && !viewModel.state.value.loading) {
                    viewModel.loadMore()
                }
            }
        })

        searchInput.setOnEditorActionListener { v, _, _ ->
            viewModel.updateQuery(v.text.toString()); viewModel.refresh(initial = true); true
        }

        swipeRefresh.setOnRefreshListener { viewModel.refresh(initial = false) }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { true }
                R.id.nav_appointments -> { startActivity(Intent(this, AppointmentsActivity::class.java)); finish(); true }
                R.id.nav_favorites -> { startActivity(Intent(this, FavoritesActivity::class.java)); finish(); true }
                R.id.nav_profile -> { startActivity(Intent(this, ProfileActivity::class.java)); finish(); true }
                else -> false
            }
        })
        bottomNav.selectedItemId = R.id.nav_home

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { finishAffinity() }
        })
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun render(state: HomeUiState) {
        swipeRefresh.isRefreshing = state.refreshing
        loadingProgress.isVisible = state.loading && !state.refreshing && state.items.isEmpty()
        emptyStateText.isVisible = !state.loading && state.items.isEmpty() && state.error == null
        errorText.isVisible = state.error != null
        errorText.text = state.error
        adapter.submitList(state.items)
    }
}
