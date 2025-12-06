package com.example.lostfinder.data.api

import com.example.lostfinder.data.model.ApiResponse
import com.example.lostfinder.data.model.member.LoginRequest
import com.example.lostfinder.data.model.member.LoginResult
import com.example.lostfinder.data.model.member.SignupRequest
import com.example.lostfinder.data.model.member.SignupResult
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MemberApi {

    @POST("/api/members/signup")
    suspend fun signup(
        @Body req: SignupRequest
    ): Response<ApiResponse<SignupResult>>

    @POST("/api/members/login")
    suspend fun login(
        @Body req: LoginRequest
    ): Response<ApiResponse<LoginResult>>
}