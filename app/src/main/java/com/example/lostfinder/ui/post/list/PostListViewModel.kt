package com.example.lostfinder.ui.post.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfinder.data.model.post.PostListItem
import com.example.lostfinder.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostListViewModel : ViewModel() {

    private val repo = PostRepository()

    private val _posts = MutableStateFlow<List<PostListItem>>(emptyList())
    val posts: StateFlow<List<PostListItem>> = _posts

    // 로딩 종료 신호
    private val _refreshDone = MutableStateFlow(false)
    val refreshDone: StateFlow<Boolean> = _refreshDone

    fun resetRefreshFlag() {
        _refreshDone.value = false
    }
    fun loadPosts() {
        viewModelScope.launch {
            try {
                val response = repo.getPosts()

                if (response.isSuccessful) {
                    val page = response.body()
                    val list = page?.content ?: emptyList()

                    _posts.value = list.map {
                        PostListItem(
                            id = it.postId,
                            title = it.title,
                            createdAt = it.createAt,
                            imageUrl = it.imageUrl
                        )
                    }
                }
            } catch (_: Exception) {
            } finally {
                // 무조건 실행됨
                _refreshDone.value = true
            }
        }
    }
}
