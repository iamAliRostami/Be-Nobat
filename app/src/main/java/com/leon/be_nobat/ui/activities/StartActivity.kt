package com.leon.be_nobat.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.leon.be_nobat.data.local.TokenManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class StartActivity : AppCompatActivity() {
    private val tokenManager: TokenManager by inject()

    val DEFAULT_LANGUAGE_TAG = "fa"
    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        /*if (AppCompatDelegate.getApplicationLocales().isEmpty) {
           AppCompatDelegate.setApplicationLocales(
               LocaleListCompat.forLanguageTags(DEFAULT_LANGUAGE_TAG)
           )
       }*/
        lifecycleScope.launch {
            val token = tokenManager.userToken.first()
            if (token != null) {
                startActivity(Intent(this@StartActivity, HomeActivity::class.java))
            } else {
                startActivity(Intent(this@StartActivity, LoginActivity::class.java))
            }
            finish()
        }
    }
}