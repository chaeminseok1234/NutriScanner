package com.example.nutriscanner.analyze

import android.content.Context
import android.util.Log
import com.example.nutriscanner.api.GPTService
import com.example.nutriscanner.model.*
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NutritionAnalyzer(context: Context) {

    private val gptService: GPTService

    // 초기화 블록에서 Retrofit 및 OkHttpClient 설정
    init {

        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create()) // JSON → 객체 자동 변환
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        // HTTP 요청에 Authorization 헤더 추가
                        val request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer ") // Bearer뒤에 API키 기입하시면 됩니당
                            .build()
                        chain.proceed(request)
                    }
                    .connectTimeout(30, TimeUnit.SECONDS)  // 서버 연결 대기 시간
                    .readTimeout(30, TimeUnit.SECONDS)     // 응답 읽기 대기 시간
                    .writeTimeout(30, TimeUnit.SECONDS)    // 요청 쓰기 대기 시간
                    .build()
            )
            .build()

        // GPTService 인터페이스 구현체 생성
        gptService = retrofit.create(GPTService::class.java)
    }


    fun getNutritionFeedback(foodList: List<String>, callback: (String) -> Unit) {
        // 대화 메시지 구성 (시스템 + 유저 입력)
        val messages = listOf(
            Message("system", "You are a helpful nutritionist."), // 시스템 역할 설정
            Message("user", buildPrompt(foodList)) // 사용자 질문 내용 생성
        )

        // GPT API에 보낼 요청 객체 생성
        val request = ChatRequest(
            model = "gpt-4",
            messages = messages,
            max_tokens = 300 // 응답 최대 토큰 수 제한
        )

        // 비동기로 API 요청
        gptService.getNutritionFeedback(request).enqueue(object : Callback<ChatResponse> {

            // 응답 성공 시
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    // 응답 본문에서 결과 텍스트 추출
                    val feedback = response.body()?.choices?.get(0)?.message?.content ?: "피드백을 받을 수 없습니다."
                    callback(feedback) // 콜백으로 피드백 전달
                    Log.d("OpenAI API", "응답 성공: ${response.body()}")
                } else {
                    // 오류 응답 처리
                    callback("API 요청에 실패했습니다. 상태 코드: ${response.code()}")
                    Log.e("OpenAI API", "응답 실패: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            // 요청 자체 실패
            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                callback("서버와 연결할 수 없습니다. 오류: ${t.message}")
                Log.e("OpenAI API", "API 호출 실패: ${t.localizedMessage}", t)
            }
        })
    }


    //gpt 프로젝트 지침처럼 넣어주는 함수
    private fun buildPrompt(foodList: List<String>): String {
        val foods = foodList.joinToString(", ")
        return """
            음식 목록: $foods
            각 음식의 탄수화물, 단백질, 지방, 나트륨, 당에 대한 영양 성분을 분석하고,
            식습관 개선 사항을 피드백 해 주세요. 각 음식에 대한 식습관 개선을 제시해주세요.
        """.trimIndent()
    }
}
