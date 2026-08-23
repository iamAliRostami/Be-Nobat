package com.leon.be_nobat.ui.activities

import android.content.Intent
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.leon.be_nobat.R
import com.leon.be_nobat.domain.model.AuthException
import com.leon.be_nobat.helpers.BaseActivity
import com.leon.be_nobat.ui.view_models.auth.AuthViewModel
import com.leon.be_nobat.ui.view_models.auth.LoginUiState
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : BaseActivity(), View.OnClickListener {
    override val layoutResourceId: Int = R.layout.activity_login
    private val authViewModel: AuthViewModel by viewModel()

    private val identityInput by lazy { findViewById<TextInputLayout>(R.id.tilIdentity) }
    private val passwordInput by lazy { findViewById<TextInputLayout>(R.id.tilPassword) }
    private val loginButton by lazy { findViewById<MaterialButton>(R.id.btnLogin) }

    override fun setupViews() {
        setToolbarTitle(true)
        findViewById<ImageButton>(R.id.btnThemeToggle).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.btnGuest).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.btnLogin).setOnClickListener(this)
        findViewById<MaterialButton>(R.id.btnLanguage).setOnClickListener(this)
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.loginState.collect { renderLoginState(it) }
            }
        }
    }

    override fun onClick(v: View?) {
        val id = v?.id
        if (id == R.id.btnThemeToggle) {
            setupThemeToggle()
        } else if (id == R.id.btnLanguage) {
            v?.let(::showLanguageMenu)
        } else if (id == R.id.btnGuest) {
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
        } else if (id == R.id.btnLogin) {
            clearInputErrors()
            authViewModel.login(
                identityInput.editText?.text?.toString().orEmpty(),
                passwordInput.editText?.text?.toString().orEmpty(),
            )
        }
    }

    private fun renderLoginState(state: LoginUiState) {
        loginButton.isEnabled = state !is LoginUiState.Loading
        loginButton.text = if (state is LoginUiState.Loading) {
            getString(R.string.logging_in)
        } else {
            getString(R.string.login)
        }
        when (state) {
            is LoginUiState.Error -> {
                val message = getString(state.error.messageResource())
                when (state.error) {
                    AuthException.InvalidIdentifier -> identityInput.error = message
                    AuthException.EmptyPassword -> passwordInput.error = message
                    else -> Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                }
            }
            is LoginUiState.Success -> {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }
            LoginUiState.Idle, LoginUiState.Loading -> Unit
        }
    }

    private fun clearInputErrors() {
        identityInput.error = null
        passwordInput.error = null
    }

    private fun showLanguageMenu(anchor: View) {
        showPopupMenu(anchor, R.menu.language_menu) { menuItemId ->
            val languageTag = when (menuItemId) {
                R.id.language_persian -> "fa"
                R.id.language_arabic -> "ar"
                R.id.language_english -> "en"
                else -> return@showPopupMenu
            }
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(languageTag)
            )
        }
    }

    private fun Throwable.messageResource(): Int = when (this) {
        AuthException.InvalidIdentifier -> R.string.error_invalid_identifier
        AuthException.EmptyPassword -> R.string.error_empty_password
        AuthException.InvalidCredentials -> R.string.error_invalid_credentials
        AuthException.TooManyRequests -> R.string.error_too_many_requests
        AuthException.ServiceUnavailable -> R.string.error_service_unavailable
        AuthException.NetworkUnavailable -> R.string.error_network_unavailable
        AuthException.RequestTimedOut -> R.string.error_request_timed_out
        else -> R.string.unexpected_error
    }
}
