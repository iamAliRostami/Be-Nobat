package com.leon.be_nobat.helpers

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.PopupMenu
import androidx.activity.enableEdgeToEdge
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.leon.be_nobat.R
import com.leon.be_nobat.data.local.ThemeManager
import com.leon.be_nobat.ui.view_models.base.MainViewModel
import org.koin.android.ext.android.inject

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private val themePreferences: ThemeManager by inject()
    private val viewModel = MainViewModel(themePreferences)
    private var isMenuVisible = false
    private var isHorizontalButtonsVisible = true

    @get:LayoutRes
    protected abstract val layoutResourceId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_base)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val container = findViewById<FrameLayout>(R.id.baseContentContainer)
        LayoutInflater.from(this).inflate(layoutResourceId, container, true)

        toolbar = findViewById(R.id.baseToolbar)
        setSupportActionBar(toolbar)

        setupViews()
        observeViewModel()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isMenuVisible) {
            menuInflater.inflate(R.menu.main_menu, menu)
            menu.findItem(R.id.action_language)?.isVisible = isHorizontalButtonsVisible
            menu.findItem(R.id.switchTheme)?.isVisible = isHorizontalButtonsVisible
            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_language -> {
                val anchor = findViewById<View>(R.id.action_language) ?: toolbar
                showLanguageMenu(anchor)
            }
            R.id.switchTheme -> setupThemeToggle()
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun getCurrentLanguage(): String {
        val locales = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales()
        if (!locales.isEmpty) {
            val tag = locales.get(0)?.language
            if (!tag.isNullOrBlank()) return tag
        }
        return "fa"
    }

    fun getCurrentLanguageLabel(): String = when (getCurrentLanguage()) {
        "en" -> "EN"
        "ar" -> "عر"
        else -> "فا"
    }

    fun setupThemeToggle() {
        val isNight =
            resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        viewModel.toggleTheme(isNight)
    }

    fun showLanguageMenu(anchor: View) {
        showPopupMenu(anchor, R.menu.language_menu) { menuItemId ->
            val languageTag = when (menuItemId) {
                R.id.language_persian -> "fa"
                R.id.language_arabic -> "ar"
                R.id.language_english -> "en"
                else -> return@showPopupMenu
            }
            viewModel.switchLanguage(languageTag)
            recreate()
        }
    }


    protected abstract fun setupViews()
    protected abstract fun observeViewModel()
    protected open fun onSearchClicked() {}
    protected open fun onFilterClicked() {}
    protected open fun onSortClicked() {}
    protected open fun onRefreshClicked() {}

    protected fun setHorizontalButtonsVisibility(visible: Boolean) {
        isHorizontalButtonsVisible = visible
        invalidateOptionsMenu()
    }

    protected fun setMenuVisibility(visible: Boolean) {
        isMenuVisible = visible
        invalidateOptionsMenu()
    }

    protected fun showPopupMenu(view: View, menuRes: Int, onMenuItemClick: (Int) -> Unit) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(menuRes, popup.menu)
        popup.setOnMenuItemClickListener { item ->
            onMenuItemClick(item.itemId)
            true
        }
        popup.show()
    }

    protected fun setToolbarTitle(title: String) {
        toolbar.title = title
        setupBaseToolbar()
    }

    protected fun setToolbarTitle(hide: Boolean) {
        if (hide)
            hideToolbar()
        else
            setupBaseToolbar()
    }

    private fun setupBaseToolbar() {
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun hideToolbar() {
        toolbar.visibility = View.GONE
    }
}