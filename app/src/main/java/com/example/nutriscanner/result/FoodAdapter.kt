package com.example.nutriscanner.result

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriscanner.R

class FoodAdapter(private val foodList: List<Food>) : RecyclerView.Adapter<FoodAdapter.FoodViewHolder>() {

    inner class FoodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val foodName: TextView = view.findViewById(R.id.foodName)
        val foodNutrients: TextView = view.findViewById(R.id.foodNutrients)
        val foodHeader: RelativeLayout = view.findViewById(R.id.foodHeader)
        val nutrientsLayout: LinearLayout = view.findViewById(R.id.nutrientsLayout)
        val expandIcon: ImageView = view.findViewById(R.id.expandIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_food_card, parent, false)
        return FoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        val food = foodList[position]

        // 음식 이름 설정
        holder.foodName.text = food.name

        // 영양성분 정보 설정
        val nutrientText = "나트륨: ${food.nutrients["나트륨"]}, 단백질: ${food.nutrients["단백질"]}, " +
                "탄수화물: ${food.nutrients["탄수화물"]}, 당: ${food.nutrients["당"]}, 지방: ${food.nutrients["지방"]}"
        holder.foodNutrients.text = nutrientText

        // 확장/축소 기능
        holder.foodHeader.setOnClickListener {
            if (holder.nutrientsLayout.visibility == View.GONE) {
                // 펼치기
                holder.nutrientsLayout.visibility = View.VISIBLE
                holder.expandIcon.setImageResource(R.drawable.ic_expand_less)
            } else {
                // 접기
                holder.nutrientsLayout.visibility = View.GONE
                holder.expandIcon.setImageResource(R.drawable.ic_expand_more)
            }
        }

        // 초기 상태는 접힌 상태로 설정
        holder.nutrientsLayout.visibility = View.GONE
        holder.expandIcon.setImageResource(R.drawable.ic_expand_more)
    }

    override fun getItemCount() = foodList.size
}