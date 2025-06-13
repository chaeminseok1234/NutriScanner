package com.example.nutriscanner.result

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.nutriscanner.R

class NutritionFeedbackActivity : AppCompatActivity() {

    private lateinit var feedbackText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nutrition_feedback)

        feedbackText = findViewById(R.id.feedbackText)

        val feedback = intent.getStringExtra("nutritionFeedback")
        feedbackText.text = feedback ?: "피드백을 받을 수 없습니다."
    }
}
