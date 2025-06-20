package com.example.nutriscanner.model

import com.google.gson.annotations.SerializedName

data class FoodResponse(
    @SerializedName("header") val header: Header,
    @SerializedName("body") val body: Body
)

data class Header(
    @SerializedName("resultCode") val code: String,
    @SerializedName("resultMsg")  val message: String
)

data class Body(
    @SerializedName("pageNo")     val pageNo: Int,
    @SerializedName("totalCount") val totalCount: Int,
    @SerializedName("numOfRows")  val numOfRows: Int,
    @SerializedName("items")      val items: List<FoodItem>
)