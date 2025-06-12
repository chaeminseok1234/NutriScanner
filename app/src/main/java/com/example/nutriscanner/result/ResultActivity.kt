package com.example.nutriscanner.result

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.nutriscanner.R

class ResultActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var totalNutrientsText: TextView
    private lateinit var gptCommentText: TextView
    private lateinit var foodImage: ImageView
    private lateinit var adapter: FoodAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        recyclerView = findViewById(R.id.foodRecyclerView)
        totalNutrientsText = findViewById(R.id.totalNutrients)
        gptCommentText = findViewById(R.id.gptComment)
        foodImage = findViewById(R.id.foodImage)

        recyclerView.layoutManager = LinearLayoutManager(this)

        val dateTime = intent.getStringExtra("dateTime")
        val foods = intent.getParcelableArrayListExtra<Food>("foods")
        val totalNutrients = intent.getSerializableExtra("totalNutrients") as Map<String, Double>
        val gptComment = intent.getStringExtra("gptComment") ?: "없음"
        val imageUri = intent.getStringExtra("imageUri")

        Glide.with(this)
            .load(imageUri)
            .into(foodImage)

        adapter = FoodAdapter(foods ?: emptyList())
        recyclerView.adapter = adapter

        val totalInfo = "나트륨: ${totalNutrients["나트륨"]}\n단백질: ${totalNutrients["단백질"]}\n" +
                "당: ${totalNutrients["당"]}\n지방: ${totalNutrients["지방"]}\n탄수화물: ${totalNutrients["탄수화물"]}"
        totalNutrientsText.text = totalInfo



        gptCommentText.text = gptComment
    }
}
