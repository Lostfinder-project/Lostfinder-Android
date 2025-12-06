package com.example.lostfinder.ui.post.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfinder.data.model.post.PostCreateRequest
import com.example.lostfinder.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PostCreateViewModel : ViewModel() {

    private val repo = PostRepository()

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState

    fun createPost(image: MultipartBody.Part?, data: RequestBody) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Loading

            val response = repo.createPost(image, data)

            if (response.isSuccessful) {
                _uploadState.value = UploadState.Success
            } else {
                _uploadState.value = UploadState.Error("업로드 실패: ${response.code()}")
            }
        }
    }

    sealed class UploadState {
        data object Idle : UploadState()
        data object Loading : UploadState()
        data object Success : UploadState()
        data class Error(val msg: String) : UploadState()
    }
}
