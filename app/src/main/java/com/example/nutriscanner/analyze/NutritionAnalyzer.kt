package com.example.nutriscanner.analyze

import com.example.nutriscanner.BuildConfig
import android.content.Context
import android.util.Log
import com.example.nutriscanner.api.GPTService
import com.example.nutriscanner.model.*
import com.google.gson.Gson
import okhttp3.OkHttpClient
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NutritionAnalyzer(context: Context) {

    private val gptService: GPTService

    init {

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .addHeader(
                                "Authorization",
                                "Bearer ${BuildConfig.OPENAI_API_KEY}"
                            )
                            .build()
                        chain.proceed(request)
                    }
                    .connectTimeout(30, TimeUnit.SECONDS)  // 연결 타임아웃
                    .readTimeout(30, TimeUnit.SECONDS)     // 읽기 타임아웃
                    .writeTimeout(30, TimeUnit.SECONDS)    // 쓰기 타임아웃
                    .build()
            )
            .build()

        gptService = retrofit.create(GPTService::class.java)
    }

    fun getNutritionFeedback(foodList: List<Map<String, Any>>, callback: (String) -> Unit) {
        val messages = listOf(
            Message("system", "You are a helpful nutritionist."),
            Message("user", buildPrompt(foodList))
        )

        val request = ChatRequest(
            model = "gpt-4",  // 또는 gpt-4.0-turbo 등
            messages = messages,
            max_tokens = 300
        )

        gptService.getNutritionFeedback(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                if (response.isSuccessful) {
                    val feedback = response.body()?.choices?.get(0)?.message?.content ?: "피드백을 받을 수 없습니다."
                    callback(feedback)
                    Log.d("OpenAI API", "응답 성공: ${response.body()}")
                } else {
                    callback("API 요청에 실패했습니다. 상태 코드: ${response.code()}")
                    Log.e("OpenAI API", "응답 실패: ${response.code()} - ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                callback("서버와 연결할 수 없습니다. 오류: ${t.message}")
                Log.e("OpenAI API", "API 호출 실패: ${t.localizedMessage}", t)
            }
        })
    }

    private fun buildPrompt(foodList: List<Map<String, Any>>): String {
        val gson = Gson()
        val foodJson = gson.toJson(foodList)

        return """
            다음 음식들의 영양정보를 아래 조건을 토대로 분석해줘:
            1) nutrients는 1회 제공량 기준이야. actualServing에 기반해서 이미 계산된 값이므로 추가 계산할 필요없어.
            2) 식습관 개선을 위한 조언만을 현재 음식 영양정보를 토대로 각 음식마다 200자 이내로 알려줘. 입력한 영양소를 다시 보여줄 필요 없어.

            음식 리스트 (JSON):
            $foodJson
        """.trimIndent()
    }
}
