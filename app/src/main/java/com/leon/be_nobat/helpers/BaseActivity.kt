package com.leon.be_nobat.helpers

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

import com.google.android.material.appbar.MaterialToolbar
import com.leon.be_nobat.R

abstract class BaseActivity : AppCompatActivity() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var btnThemeToggle: ImageButton

    @get:LayoutRes
    protected abstract val layoutResourceId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)

        // مدیریت راست‌چین بودن همیشگی
        window.decorView.layoutDirection = android.view.View.LAYOUT_DIRECTION_RTL

        val container = findViewById<FrameLayout>(R.id.baseContentContainer)
        LayoutInflater.from(this).inflate(layoutResourceId, container, true)

        setupBaseToolbar()
        setupThemeToggle() // راه‌اندازی دکمه تغییر تم

        setupViews()
        observeViewModel()
    }

    private fun setupBaseToolbar() {
        toolbar = findViewById(R.id.baseToolbar)
        toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        findViewById<ImageButton>(R.id.btnSearch).setOnClickListener { onSearchClicked() }
        findViewById<ImageButton>(R.id.btnFilter).setOnClickListener { onFilterClicked() }
        findViewById<ImageButton>(R.id.btnSort).setOnClickListener { onSortClicked() }
        findViewById<ImageButton>(R.id.btnRefresh).setOnClickListener { onRefreshClicked() }
    }

    private fun setupThemeToggle() {
        btnThemeToggle = findViewById(R.id.btnThemeToggle)

        // ۱. بررسی تم فعلی سیستم برای نمایش آیکون صحیح در بدو ورود
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        /*if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            btnThemeToggle.setImageResource(R.drawable.ic_light_mode)
        } else {
            btnThemeToggle.setImageResource(R.drawable.ic_dark_mode)
        }*/

        // ۲. هندل کردن کلیک روی دکمه و سوئیچ کردن تم
        btnThemeToggle.setOnClickListener {
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                // اگر شب است، به حالت روز برو
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                // اگر روز است، به حالت شب برو
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
        }
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
}