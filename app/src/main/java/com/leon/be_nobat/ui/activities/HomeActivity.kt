package com.leon.be_nobat.ui.activities

import com.leon.be_nobat.R
import com.leon.be_nobat.helpers.BaseActivity

class HomeActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_home

    override fun setupViews() {
        setToolbarTitle(getString(R.string.orders_title))
        setMenuVisibility(false)
    }

    override fun observeViewModel() {

    }
}
