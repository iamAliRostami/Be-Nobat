package com.leon.be_nobat.helpers

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.appbar.MaterialToolbar
import com.leon.be_nobat.R
import com.leon.be_nobat.data.local.ThemeManager
import com.leon.be_nobat.ui.view_models.base.MainViewModel
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private val themePreferences: ThemeManager by inject()
    private val viewModel = MainViewModel(themePreferences)

    @get:LayoutRes
    protected abstract val layoutResourceId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        // مدیریت راست‌چین بودن همیشگی
//        window.decorView.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL

        val container = findViewById<FrameLayout>(R.id.baseContentContainer)
        LayoutInflater.from(this).inflate(layoutResourceId, container, true)

        toolbar = findViewById(R.id.baseToolbar)

        setupViews()
        observeViewModel()
    }

    private fun setupBaseToolbar() {
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageButton>(R.id.btnSearch).setOnClickListener { onSearchClicked() }
        findViewById<ImageButton>(R.id.btnFilter).setOnClickListener { onFilterClicked() }
        findViewById<ImageButton>(R.id.btnSort).setOnClickListener { onSortClicked() }
        findViewById<ImageButton>(R.id.btnRefresh).setOnClickListener { onRefreshClicked() }
        setupThemeToggle(findViewById(R.id.btnThemeToggle))
    }

    fun setupThemeToggle(btnThemeToggle: View) {
        btnThemeToggle.setOnClickListener {
            val isNight =
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            viewModel.toggleTheme(isNight)
        }
    }

    private fun hideToolbar() {
        toolbar.visibility = GONE
    }

    protected abstract fun setupViews()
    protected abstract fun observeViewModel()

    protected open fun onSearchClicked() {}
    protected open fun onFilterClicked() {}
    protected open fun onSortClicked() {}
    protected open fun onRefreshClicked() {}

    protected fun setToolbarTitle(title: String) {
        toolbar.title = title
    }

    protected fun setToolbarTitle(title: String?, hide: Boolean) {
        if (hide)
            hideToolbar()
        else
            setupBaseToolbar()
    }
}