package com.example.lostfinder.data.api

import com.example.lostfinder.data.model.ApiResponse
import com.example.lostfinder.data.model.PageResponse
import com.example.lostfinder.data.model.post.ContactResponse
import com.example.lostfinder.data.model.post.PostCreateResult
import com.example.lostfinder.data.model.post.PostDetailResponse
import com.example.lostfinder.data.model.post.PostSummaryResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface PostApi {

    @Multipart
    @POST("/api/posts")
    suspend fun createPost(
        @Part image: MultipartBody.Part?,
        @Part("data") data: RequestBody      // 🔥 key 이름 "data" 로 통일
    ): Response<ApiResponse<PostCreateResult>>

    @GET("/api/posts")
    suspend fun getPosts(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<PostSummaryResponse>>

    @GET("/api/posts/{id}")
    suspend fun getPostDetail(
        @Path("id") id: Long
    ): Response<ApiResponse<PostDetailResponse>>

    @GET("/api/posts/{id}/contact")
    suspend fun getContact(
        @Path("id") id: Long
    ): Response<ApiResponse<ContactResponse>>
}
