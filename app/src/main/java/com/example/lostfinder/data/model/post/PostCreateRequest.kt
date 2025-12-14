package com.example.lostfinder.data.model.post

data class PostCreateRequest(
    val title: String,
    val content: String,
    val foundLocation: String,
    val categoryId: Long,
    val lat: Double, // 위도
    val lng: Double  // 경도
)
