package com.example.lostfinder.data.model.member

data class SignupRequest(
    val username: String,
    val password: String,
    val name: String,
    val phone: String,
    val email: String
)
