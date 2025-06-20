package com.example.nutriscanner.api

import com.example.nutriscanner.model.ChatRequest
import com.example.nutriscanner.model.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface GPTService {
    @POST("chat/completions")
    @Headers("Content-Type: application/json")
    fun getNutritionFeedback(@Body request: ChatRequest): Call<ChatResponse>
}
