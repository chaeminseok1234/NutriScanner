package com.example.nutriscanner.result

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriscanner.R

class FoodAdapter(private val foodList: List<Food>) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodName: TextView = view.findViewById(R.id.foodName)
        val nutrientInfo: TextView = view.findViewById(R.id.nutrientInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food_card, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]
        holder.foodName.text = food.name
        holder.nutrientInfo.text = "나트륨: ${food.nutrients["나트륨"]}, 단백질: ${food.nutrients["단백질"]}, " +
                "탄수화물: ${food.nutrients["탄수화물"]}, 당: ${food.nutrients["당"]}, 지방: ${food.nutrients["지방"]}"
    }

    override fun getItemCount() = foodList.size
}
