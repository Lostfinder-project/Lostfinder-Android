package com.example.lostfinder.data.api

import com.example.lostfinder.data.model.ApiResponse
import com.example.lostfinder.data.model.category.Category
import retrofit2.http.GET

interface CategoryApi {
    @GET("/api/categories")
    suspend fun getCategories(): ApiResponse<List<Category>>
}