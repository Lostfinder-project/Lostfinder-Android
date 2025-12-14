package com.example.lostfinder.data.model.post

data class PostDetailResponse(
    val id: Long,
    val title: String,
    val content: String,
    val imageUrl: String?,
    val foundLocation: String,
    val writerName: String,
    val writerPhone: String,
    val lat: Double?,
    val lng: Double?
)
