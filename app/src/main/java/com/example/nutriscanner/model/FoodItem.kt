package com.example.nutriscanner.model

import com.google.gson.annotations.SerializedName

data class FoodItem(
    @SerializedName("FOOD_NM_KR") val name: String,
    @SerializedName("AMT_NUM1")   val kcal: Double,
    @SerializedName("AMT_NUM6")   val carbs: Double,
    @SerializedName("AMT_NUM3")   val protein: Double,
    @SerializedName("AMT_NUM4")   val fat: Double,
    @SerializedName("AMT_NUM7")   val sugar: Double,
    @SerializedName("AMT_NUM13")  val sodium: Double,
    @SerializedName("SERVING_SIZE") val servingSize: String,
    @SerializedName("Z10500")       val actualServing: String?
)
