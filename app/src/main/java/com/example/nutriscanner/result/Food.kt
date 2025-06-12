package com.example.nutriscanner.result

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Food(
    val name: String,
    val nutrients: Map<String, Double>
) : Parcelable
