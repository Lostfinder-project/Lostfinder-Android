package com.example.lostfinder.data.model.post

data class PostSummaryResponse(
    val postId: Long,
    val title: String,
    val imageUrl: String?,
    val category: String,
    val createAt: String
)