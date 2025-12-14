package com.example.lostfinder.ui.post.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lostfinder.data.model.category.Category
import com.example.lostfinder.data.model.post.PostListItem
import com.example.lostfinder.data.repository.CategoryRepository
import com.example.lostfinder.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PostListViewModel : ViewModel() {

    private val postRepo = PostRepository()
    private val categoryRepo = CategoryRepository()

    private val _posts = MutableStateFlow<List<PostListItem>>(emptyList())
    val posts: StateFlow<List<PostListItem>> = _posts

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var selectedCategoryId: Long? = null

    init {
        loadCategories()
        loadPosts(0)
    }

    fun setCategory(categoryId: Long?) {
        selectedCategoryId = categoryId
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val res = categoryRepo.getCategories()
                _categories.value = res.data ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadPosts(page: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = postRepo.getPosts(
                    page = page,
                    categoryId = selectedCategoryId
                )

                if (response.isSuccessful) {
                    val body = response.body()

                    _currentPage.value = page
                    _totalPages.value = body?.totalPages ?: 1

                    _posts.value = body?.content?.map {
                        PostListItem(
                            id = it.postId,
                            title = it.title,
                            createdAt = it.createAt,
                            imageUrl = it.imageUrl
                        )
                    } ?: emptyList()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
