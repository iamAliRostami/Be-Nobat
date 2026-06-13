package com.leon.be_nobat.ui.activities

import android.widget.ImageButton
import com.leon.be_nobat.R
import com.leon.be_nobat.helpers.BaseActivity

class LoginActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_login

    override fun setupViews() {
        setToolbarTitle(true)
        findViewById<ImageButton>(R.id.btnThemeToggle).setOnClickListener {
            setupThemeToggle()
        }
    }

    override fun observeViewModel() {
    }
}