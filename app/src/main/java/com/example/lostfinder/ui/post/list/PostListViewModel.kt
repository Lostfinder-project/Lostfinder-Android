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

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    fun loadPosts(page: Int = 0) {
        viewModelScope.launch {
            try {
                val response = repo.getPosts(page)

                if (response.isSuccessful) {
                    val body = response.body()

                    _currentPage.value = page
                    _totalPages.value = body?.totalPages ?: 1

                    val list = body?.content ?: emptyList()

                    _posts.value = list.map {
                        PostListItem(
                            id = it.postId,
                            title = it.title,
                            createdAt = it.createAt,
                            imageUrl = it.imageUrl
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
