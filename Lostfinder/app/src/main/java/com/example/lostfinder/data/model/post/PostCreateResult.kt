package com.example.lostfinder.data.model.post

data class PostCreateResult(
    val postId: Long,
    val title: String,
    val imageUrl: String?
)