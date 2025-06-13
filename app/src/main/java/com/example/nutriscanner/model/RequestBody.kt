package com.example.nutriscanner.model

data class RequestBody(
    val model: String,  // 사용할 모델의 이름 (예: "text-davinci-003")
    val prompt: String, // GPT에게 전달할 질문
    val max_tokens: Int,  // 응답에서 받을 최대 토큰 수
    val temperature: Double  // 창의성 정도, 0.0 ~ 1.0 사이
)