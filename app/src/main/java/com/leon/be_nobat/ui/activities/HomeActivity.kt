package com.leon.be_nobat.ui.activities

import com.leon.be_nobat.R
import com.leon.be_nobat.helpers.BaseActivity

class HomeActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_home

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }*/

    override fun setupViews() {
        setToolbarTitle("لیست سفارشات")
    }

    override fun observeViewModel() {

    }
}