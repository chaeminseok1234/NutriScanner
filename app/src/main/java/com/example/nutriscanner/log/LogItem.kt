package com.example.nutriscanner.log

import android.os.Parcelable
import com.example.nutriscanner.result.Food
import kotlinx.parcelize.Parcelize

@Parcelize
data class LogItem(
    val id: String,
    val dateTime: String,
    val imageUri: String,
    val foods: List<Food>,
    val totalNutrients: Map<String, Double>,
    val gptComment: String
) : Parcelable
