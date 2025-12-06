package com.example.lostfinder.ui.post.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfinder.data.model.post.PostDetailResponse
import com.example.lostfinder.data.model.post.ContactResponse
import com.example.lostfinder.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostDetailViewModel : ViewModel() {

    private val repository = PostRepository()

    private val _state = MutableStateFlow<PostDetailState>(PostDetailState.Loading)
    val state: StateFlow<PostDetailState> = _state

    private val _contactState = MutableStateFlow<ContactState>(ContactState.None)
    val contactState: StateFlow<ContactState> = _contactState

    /** 게시글 상세 조회 */
    fun loadPost(id: Long) {
        viewModelScope.launch {
            _state.value = PostDetailState.Loading

            try {
                val response = repository.getPostDetail(id)

                if (response.isSuccessful) {
                    val body = response.body()?.data
                    if (body != null) {
                        _state.value = PostDetailState.Success(body)
                    } else {
                        _state.value = PostDetailState.Error("데이터 없음")
                    }
                } else {
                    _state.value = PostDetailState.Error("서버 오류 ${response.code()}")
                }

            } catch (e: Exception) {
                _state.value = PostDetailState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }

    /** 작성자 연락처 조회 */
    fun loadContact(id: Long) {
        viewModelScope.launch {
            try {
                val response = repository.getContact(id)

                if (response.isSuccessful) {
                    val body = response.body()?.data
                    if (body != null) {
                        _contactState.value = ContactState.Success(body)
                    } else {
                        _contactState.value = ContactState.Error("연락처 없음")
                    }
                } else {
                    _contactState.value = ContactState.Error("서버 오류 ${response.code()}")
                }

            } catch (e: Exception) {
                _contactState.value = ContactState.Error(e.message ?: "알 수 없는 오류")
            }
        }
    }
    fun resetContactState() {
        _contactState.value = ContactState.None
    }


    /** 게시글 상세 상태 */
    sealed class PostDetailState {
        object Loading : PostDetailState()
        data class Success(val data: PostDetailResponse) : PostDetailState()
        data class Error(val msg: String) : PostDetailState()
    }

    /** 연락처 조회 상태 */
    sealed class ContactState {
        object None : ContactState()
        data class Success(val data: ContactResponse) : ContactState()
        data class Error(val msg: String) : ContactState()
    }
}

