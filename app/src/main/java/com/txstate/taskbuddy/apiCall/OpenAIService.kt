package com.txstate.taskbuddy.apiCall

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface OpenAIService {
    @Headers(
        "Content-Type: application/json",
        "Authorization: Bearer ${ApiConstants.OPENAI_API_KEY}" // ✅ Add "Bearer "
    )
    @POST("/v1/chat/completions")
    suspend fun getCompletion(@Body request: OpenAIRequest): OpenAIResponse
}
