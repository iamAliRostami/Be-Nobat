package com.leon.be_nobat.ui.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.leon.be_nobat.data.local.TokenManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class StartActivity : AppCompatActivity() {
    companion object {
        private const val DEFAULT_LANGUAGE_TAG = "fa"
    }

    private val tokenManager: TokenManager by inject()

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setDefaultLocaleIfNeeded()
        lifecycleScope.launch {
            startActivity(Intent(this@StartActivity, com.leon.be_nobat.ui.MainActivity::class.java))
            finish()
        }
    }

    private fun setDefaultLocaleIfNeeded() {
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(DEFAULT_LANGUAGE_TAG)
            )
        }
    }
}