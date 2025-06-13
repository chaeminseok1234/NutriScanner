package com.example.nutriscanner.log

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriscanner.R
import com.example.nutriscanner.result.Food
import com.example.nutriscanner.result.ResultActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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
        val userId = "0n4FL4JO76Y5PocMIiaATvpOZTh2"

        // 로그 컬렉션에서 날짜별로 데이터를 불러옵니다. 날짜 문자열을 문서 ID로 사용
        db.collection("users")
            .document(userId)
            .collection("logs")
            .get()
            .addOnSuccessListener { result ->
                Log.d("skrskr", "유저 로그 가져오기 성공: ${result.size()}개")

                val logList = result.map { doc ->
                    val foods = (doc.get("foods") as? List<Map<String, Any>>)?.map { foodMap ->
                        val name = foodMap["name"] as? String ?: ""
                        val nutrients = foodMap["nutrients"] as? Map<String, Double> ?: emptyMap()
                        Food(name, nutrients)
                    } ?: emptyList()

                    // document ID (날짜)를 가져오고, 다른 데이터들 가져오기
                    LogItem(
                        id = doc.id,  // 문서 ID를 id로 사용
                        dateTime = doc.id,  // 문서 ID가 날짜를 나타내므로 동일하게 설정
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
                        putParcelableArrayListExtra("foods", ArrayList(item.foods))
                        putExtra("totalNutrients", HashMap(item.totalNutrients))
                        putExtra("gptComment", item.gptComment)
                    }

                    startActivity(intent)
                }

                recyclerView.adapter = adapter
            }
            .addOnFailureListener {
                Log.e("skrskr", "로그 불러오기 실패: ${it.message}")
            }
    }
}
