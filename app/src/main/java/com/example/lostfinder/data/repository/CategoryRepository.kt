package com.example.lostfinder.data.repository

import com.lostfinder.app.data.api.RetrofitInstance

class CategoryRepository {

    private val api = RetrofitInstance.categoryApi

    suspend fun getCategories() = api.getCategories()
}
