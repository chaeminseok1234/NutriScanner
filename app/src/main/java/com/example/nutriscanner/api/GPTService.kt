package com.example.nutriscanner.api

import com.example.nutriscanner.model.ChatRequest
import com.example.nutriscanner.model.ChatResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

//POST 요청으로 OpenAI GPT에 대화를 요청하고, 영양 분석 결과를 응답받는 함수
interface GPTService {
    @POST("chat/completions")
    @Headers("Content-Type: application/json")
    fun getNutritionFeedback(@Body request: ChatRequest): Call<ChatResponse>
}
