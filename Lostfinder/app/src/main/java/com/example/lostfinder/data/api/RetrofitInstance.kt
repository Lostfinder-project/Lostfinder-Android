package com.lostfinder.app.data.api

import com.example.lostfinder.data.api.MemberApi
import com.example.lostfinder.data.api.PostApi
import com.example.lostfinder.util.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    //http://10.0.2.2:8080
    private const val BASE_URL = "http://192.168.0.13:8080/"

    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val original = chain.request()
            val builder = original.newBuilder()

            val token = TokenManager.token
            if (!token.isNullOrEmpty()) {
                builder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(builder.build())
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val memberApi: MemberApi = retrofit.create(MemberApi::class.java)
    val postApi: PostApi = retrofit.create(PostApi::class.java)
}
