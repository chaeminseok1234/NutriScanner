package com.example.nutriscanner.api

import com.example.nutriscanner.model.FoodResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface FoodApiService {
    @GET("getFoodNtrCpntDbInq02")
    suspend fun getFoods(
        @Query("serviceKey", encoded = true) serviceKey: String,
        @Query("type")       type: String = "json",
        @Query("FOOD_NM_KR", encoded = false) foodName: String,
        @Query("pageNo")     pageNo: Int    = 1,
        @Query("numOfRows")  numOfRows: Int = 5
    ): Response<FoodResponse>
}