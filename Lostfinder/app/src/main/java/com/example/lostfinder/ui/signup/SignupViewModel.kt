// com.example.lostfinder.ui.signup.SignupViewModel
package com.example.lostfinder.ui.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfinder.data.model.member.SignupRequest
import com.example.lostfinder.data.repository.MemberRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SignupViewModel : ViewModel() {

    private val repo = MemberRepository()

    private val _state = MutableStateFlow<SignupState>(SignupState.Idle)
    val state: StateFlow<SignupState> = _state

    fun signup(username: String, password: String, nickname: String, phone: String, email: String) {
        viewModelScope.launch {

            _state.value = SignupState.Loading

            val req = SignupRequest(
                username = username,
                password = password,
                name = nickname,   // 🔥 nickname -> name 으로 매핑
                phone = phone,
                email = email
            )

            val response = repo.signup(req)

            if (response.isSuccessful) {
                _state.value = SignupState.Success
            } else {
                _state.value = SignupState.Error("회원가입 실패 (${response.code()})")
            }
        }
    }

    sealed class SignupState {
        data object Idle : SignupState()
        data object Loading : SignupState()
        data object Success : SignupState()
        data class Error(val msg: String) : SignupState()
    }
}
