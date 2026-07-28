package com.leon.be_nobat.ui.activities

import android.content.Intent
import android.view.View
import android.widget.ImageButton
import com.google.android.material.button.MaterialButton
import com.leon.be_nobat.R
import com.leon.be_nobat.helpers.BaseActivity

class LoginActivity : BaseActivity(), View.OnClickListener {
    override val layoutResourceId: Int = R.layout.activity_login

    override fun setupViews() {
        setToolbarTitle(true)
        findViewById<ImageButton>(R.id.btnThemeToggle).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.btnGuest).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener(this)
    }

    override fun observeViewModel() {
    }

    override fun onClick(v: View?) {
        val id = v?.id
        if (id == R.id.btnThemeToggle) {
            setupThemeToggle()
        } else if (id == R.id.btnGuest) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        } else if (id == R.id.btnLogin) {
            startActivity(Intent(this@LoginActivity, ExampleActivity::class.java))
        }
    }
}