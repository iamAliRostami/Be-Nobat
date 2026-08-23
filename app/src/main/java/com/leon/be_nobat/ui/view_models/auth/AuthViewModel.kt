package com.leon.be_nobat.ui.view_models.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.leon.be_nobat.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed interface LoginUiState {
    data object Idle : LoginUiState
    data object Loading : LoginUiState
    data class Success(val userName: String) : LoginUiState
    data class Error(val error: Throwable) : LoginUiState
}

class AuthViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val loginState: StateFlow<LoginUiState> = _loginState

    fun login(identity: String, password: String) {
        if (_loginState.value == LoginUiState.Loading) return
        viewModelScope.launch {
            _loginState.value = LoginUiState.Loading
            loginUseCase(identity, password)
                .onSuccess { user ->
                    _loginState.value = LoginUiState.Success(user.name)
                }
                .onFailure { error ->
                    _loginState.value = LoginUiState.Error(error)
                }
        }
    }
}
