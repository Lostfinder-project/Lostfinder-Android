package com.example.lostfinder.util

object TokenManager {
    var token: String? = null

    fun saveToken(t: String) {
        token = t
    }

    fun clearToken() {
        token = null
    }
}

