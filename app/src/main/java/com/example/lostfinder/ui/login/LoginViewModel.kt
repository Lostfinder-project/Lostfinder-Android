package com.example.lostfinder.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfinder.data.model.member.LoginRequest
import com.example.lostfinder.data.repository.MemberRepository
import com.example.lostfinder.util.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val repo = MemberRepository()

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _loginState.value = LoginState.Loading

            val response = repo.login(LoginRequest(username, password))

            if (response.isSuccessful) {
                val body = response.body()
                val token = body?.data?.accessToken

                if (!token.isNullOrEmpty()) {
                    TokenManager.saveToken(token)
                    _loginState.value = LoginState.Success
                } else {
                    _loginState.value = LoginState.Error("토큰이 없습니다.")
                }
            } else {
                _loginState.value = LoginState.Error("로그인 실패 (${response.code()})")
            }
        }
    }

    sealed class LoginState {
        data object Idle : LoginState()
        data object Loading : LoginState()
        data object Success : LoginState()
        data class Error(val message: String) : LoginState()
    }
}
