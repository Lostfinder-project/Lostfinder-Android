package com.example.lostfinder.data.repository

import com.lostfinder.app.data.api.RetrofitInstance
import okhttp3.MultipartBody
import okhttp3.RequestBody

class PostRepository {

    private val api = RetrofitInstance.postApi

    suspend fun getPosts(page: Int = 0, size: Int = 10) =
        api.getPosts(page, size)

    suspend fun getPostDetail(id: Long) =
        api.getPostDetail(id)

    suspend fun createPost(image: MultipartBody.Part?, data: RequestBody) =
        api.createPost(image, data)

    suspend fun getContact(id: Long) =
        api.getContact(id)
}
