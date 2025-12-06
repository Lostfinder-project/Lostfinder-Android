package com.example.lostfinder.data.repository

import com.example.lostfinder.data.model.member.LoginRequest
import com.example.lostfinder.data.model.member.SignupRequest
import com.lostfinder.app.data.api.RetrofitInstance

class MemberRepository {

    private val api = RetrofitInstance.memberApi

    suspend fun signup(req: SignupRequest) = api.signup(req)

    suspend fun login(req: LoginRequest) = api.login(req)
}
