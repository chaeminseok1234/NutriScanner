package com.example.nutriscanner.result

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.nutriscanner.R
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ResultActivity : AppCompatActivity() {

    // 소수 둘째자리 포맷 함수
    private fun format2(v: Any?): String {
        val d = when(v) {
            is Number -> v.toDouble()
            is String -> v.toDoubleOrNull() ?: 0.0
            else      -> 0.0
        }
        return String.format(Locale.getDefault(), "%.2f", d)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        // Intent로부터 uid, timestamp 받기
        val uid       = intent.getStringExtra("uid") ?: return
        val timestamp = intent.getStringExtra("timestamp") ?: return

        // 뷰 참조
        val foodImage        = findViewById<ImageView>(R.id.foodImage)
        val container        = findViewById<LinearLayout>(R.id.foodListContainer)
        val gptCommentTv     = findViewById<TextView>(R.id.gptComment)

        // 전체 테이블 셀 바인딩
        val tvTotalCarbs   = findViewById<TextView>(R.id.tvTotalCarbs)
        val tvTotalProtein = findViewById<TextView>(R.id.tvTotalProtein)
        val tvTotalFat     = findViewById<TextView>(R.id.tvTotalFat)
        val tvTotalSugar   = findViewById<TextView>(R.id.tvTotalSugar)
        val tvTotalSodium  = findViewById<TextView>(R.id.tvTotalSodium)

        // Firestore 조회
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("logs")
            .document(timestamp)
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) return@addOnSuccessListener

                // 1) 이미지 URI
                doc.getString("imageUri")?.let { uri ->
                    Glide.with(this)
                        .load(uri)
                        .into(foodImage)
                }

                // 2) food 리스트
                @Suppress("UNCHECKED_CAST")
                val rawList = doc.get("food") as? List<Map<String,Any>> ?: emptyList()
                for (food in rawList) {
                    val itemView = layoutInflater.inflate(R.layout.item_expandable_food, container, false)

                    // header
                    val header   = itemView.findViewById<LinearLayout>(R.id.headerLayout)
                    val arrow    = itemView.findViewById<ImageView>(R.id.arrowIcon)
                    val detail   = itemView.findViewById<TableLayout>(R.id.detailLayout)

                    // 값 TextView들
                    val tvCarbs   = itemView.findViewById<TextView>(R.id.tvCarbs)
                    val tvProtein = itemView.findViewById<TextView>(R.id.tvProtein)
                    val tvFat     = itemView.findViewById<TextView>(R.id.tvFat)
                    val tvSugar   = itemView.findViewById<TextView>(R.id.tvSugar)
                    val tvSodium  = itemView.findViewById<TextView>(R.id.tvSodium)

                    // 이름
                    itemView.findViewById<TextView>(R.id.foodName).text =
                        food["name"] as? String ?: "이름 없음"

                    // 영양소 맵
                    @Suppress("UNCHECKED_CAST")
                    val nut = food["nutrients"] as? Map<String,Any> ?: emptyMap()

                    // 소수 둘째자리 포맷 후 채우기
                    tvCarbs  .text = format2(nut["탄수화물"])
                    tvProtein.text = format2(nut["단백질"])
                    tvFat    .text = format2(nut["지방"])
                    tvSugar  .text = format2(nut["당류"])
                    tvSodium .text = format2(nut["나트륨"])

                    // 접기/펼치기 토글
                    header.setOnClickListener {
                        if (detail.visibility == View.VISIBLE) {
                            detail.visibility = View.GONE
                            arrow.setImageResource(R.drawable.ic_expand_more)
                        } else {
                            detail.visibility = View.VISIBLE
                            arrow.setImageResource(R.drawable.ic_expand_less)
                        }
                    }

                    container.addView(itemView)
                }

                // 3) totalNutrients
                @Suppress("UNCHECKED_CAST")
                val totalNut = doc.get("totalNutrients") as? Map<String,Any> ?: emptyMap()
                // 테이블에 바로 숫자 세팅
                tvTotalCarbs  .text = format2(totalNut["탄수화물"])
                tvTotalProtein.text = format2(totalNut["단백질"])
                tvTotalFat    .text = format2(totalNut["지방"])
                tvTotalSugar  .text = format2(totalNut["당류"])
                tvTotalSodium .text = format2(totalNut["나트륨"])


                // 4) GPT 코멘트
                gptCommentTv.text = doc.getString("gptComment") ?: ""
            }
            .addOnFailureListener { e ->
                Log.e("ResultActivity", "로그 불러오기 실패", e)
            }
    }
}
