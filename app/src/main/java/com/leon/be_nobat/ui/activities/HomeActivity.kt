package com.leon.be_nobat.ui.activities

import com.leon.be_nobat.R
import com.leon.be_nobat.helpers.BaseActivity

class HomeActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_home

    override fun setupViews() {
        setToolbarTitle("لیست سفارشات")
        setMenuVisibility(false)
    }

    override fun observeViewModel() {

    }
}