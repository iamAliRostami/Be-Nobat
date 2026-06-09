package com.leon.be_nobat.ui.view_models.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leon.be_nobat.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _loginState = MutableStateFlow<String>("در انتظار ورود...")
    val loginState: StateFlow<String> = _loginState

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            loginUseCase(email, pass)
                .onSuccess { user ->
                    _loginState.value = "خوش آمدید ${user.name}"
                }
                .onFailure { error ->
                    _loginState.value = "خطا: ${error.message}"
                }
        }
    }
}