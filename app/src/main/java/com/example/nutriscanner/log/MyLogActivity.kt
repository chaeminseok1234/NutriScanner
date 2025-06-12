package com.example.nutriscanner.log

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriscanner.R
import com.example.nutriscanner.result.Food
import com.example.nutriscanner.result.ResultActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class MyLogActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyLogAdapter
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mylog)

        db = FirebaseFirestore.getInstance()
        recyclerView = findViewById(R.id.myLogRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchDataFromFirestore()
    }

    private fun fetchDataFromFirestore() {
        db.collection("logs")
            .get()
            .addOnSuccessListener { result ->
                Log.d("skrskr", "Firestore fetch 성공, ${result.size()}개 문서 있음")

                val logList = result.map { doc ->
                    // Food 객체 변환: Map<String, Any> -> Food
                    val foods = (doc.get("foods") as? List<Map<String, Any>>)?.map { foodMap ->
                        val name = foodMap["name"] as? String ?: ""
                        val nutrients = foodMap["nutrients"] as? Map<String, Double> ?: emptyMap()
                        Food(name, nutrients)
                    } ?: emptyList()

                    LogItem(
                        id = doc.getString("id") ?: doc.id,
                        dateTime = doc.getString("dateTime") ?: "",
                        imageUri = doc.getString("imageUri") ?: "",
                        foods = foods,
                        totalNutrients = doc.get("totalNutrients") as? Map<String, Double> ?: emptyMap(),
                        gptComment = doc.getString("gptComment") ?: ""
                    )
                }

                adapter = MyLogAdapter(logList.toMutableList()) { item ->
                    Toast.makeText(this, "${item.dateTime} 선택됨", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, ResultActivity::class.java).apply {
                        putExtra("dateTime", item.dateTime)
                        putExtra("imageUri", item.imageUri)
                        putParcelableArrayListExtra("foods", ArrayList(item.foods))  // 이제 Parcelable로 보내기
                        putExtra("totalNutrients", HashMap(item.totalNutrients))
                        putExtra("gptComment", item.gptComment)
                    }

                    startActivity(intent)
                }
                recyclerView.adapter = adapter
            }
            .addOnFailureListener {
                Log.e("skrskr", "Firestore fetch 실패: ${it.message}")
            }
    }

}
