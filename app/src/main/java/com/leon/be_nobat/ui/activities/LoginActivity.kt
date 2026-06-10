package com.leon.be_nobat.ui.activities

import android.widget.ImageButton
import com.leon.be_nobat.R
import com.leon.be_nobat.helpers.BaseActivity

class LoginActivity : BaseActivity() {
    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }*/
    override val layoutResourceId: Int = R.layout.activity_login

    override fun setupViews() {
        setToolbarTitle(null,true)
//        hideToolbar()
        /*findViewById<ImageButton>(R.id.btnThemeToggle).setOnClickListener {
            val isNight =
                resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
            viewModel.toggleTheme(isNight)
        }*/
        setupThemeToggle(findViewById<ImageButton>(R.id.btnThemeToggle))
    }

    override fun observeViewModel() {
    }
}