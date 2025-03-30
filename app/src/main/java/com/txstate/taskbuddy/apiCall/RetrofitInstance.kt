package com.txstate.taskbuddy.apiCall

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

import com.google.gson.GsonBuilder


object RetrofitInstance {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    private val gson = GsonBuilder()
        .setLenient() // ✅ Allow relaxed JSON parsing
        .create()

    private val retrofit = Retrofit.Builder()
        .baseUrl(ApiConstants.BASE_URL) // ✅ Ensure correct base URL
        .addConverterFactory(GsonConverterFactory.create(gson)) // ✅ Use proper JSON serialization
        .client(client)
        .build()

    val api: OpenAIService by lazy {
        retrofit.create(OpenAIService::class.java)
    }
}


