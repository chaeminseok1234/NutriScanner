package com.example.nutriscanner.api
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer

object ApiClient {
    private const val BASE_URL = "https://apis.data.go.kr/1471000/FoodNtrCpntDbInfo02/"

    private val gson = GsonBuilder()
        .setLenient()
        .registerTypeAdapter(Double::class.java, JsonDeserializer { json, _, _ ->
            json.asString.replace(",", "").toDoubleOrNull() ?: 0.0
        })
        .create()

    // 로깅 인터셉터
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val service: FoodApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(FoodApiService::class.java)
    }
}
