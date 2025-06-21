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
        recyclerView.layoutManager = LinearLayoutManager(this) // 세로 방향 리스트

        fetchDataFromFirestore()
    }

    
      
    //Firestore에서 사용자 로그 데이터 가져옴
    private fun fetchDataFromFirestore() {
        val userId = "0n4FL4JO76Y5PocMIiaATvpOZTh2" // 임시 사용자 ID (테스트용)

        // 해당 사용자의 logs 컬렉션에서 모든 문서 가져오기
        db.collection("users")
            .document(userId)
            .collection("logs")
            .get()
            .addOnSuccessListener { result ->
                Log.d("skrskr", "유저 로그 가져오기 성공: ${result.size()}개")

                // 가져온 문서들을 LogItem 리스트로 변환
                val logList = result.map { doc ->
                    val foods = (doc.get("foods") as? List<Map<String, Any>>)?.map { foodMap ->
                        val name = foodMap["name"] as? String ?: "" // 음식 이름
                        val nutrients = foodMap["nutrients"] as? Map<String, Double> ?: emptyMap() // 영양소 정보
                        Food(name, nutrients) // Food 객체 생성
                    } ?: emptyList()

                    // 각 로그 문서의 정보를 LogItem 객체로 구성
                    LogItem(
                        id = doc.id,
                        dateTime = doc.id, // 문서 ID가 날짜 문자열이므로 그대로 사용
                        imageUri = doc.getString("imageUri") ?: "",
                        foods = foods,
                        totalNutrients = doc.get("totalNutrients") as? Map<String, Double> ?: emptyMap(),
                        gptComment = doc.getString("gptComment") ?: ""
                    )
                }

                // 어댑터 생성 및 클릭 리스너 등록
                adapter = MyLogAdapter(logList.toMutableList()) { item ->
                    Toast.makeText(this, "${item.dateTime} 선택됨", Toast.LENGTH_SHORT).show()
                    // ResultActivity로 데이터 전달 후 이동
                    val intent = Intent(this, ResultActivity::class.java).apply {
                        putExtra("dateTime", item.dateTime)
                        putExtra("imageUri", item.imageUri)
                        putParcelableArrayListExtra("foods", ArrayList(item.foods)) // Food 리스트 전달
                        putExtra("totalNutrients", HashMap(item.totalNutrients))   // 전체 영양소 Map 전달
                        putExtra("gptComment", item.gptComment) // GPT 코멘트 전달
                    }

                    startActivity(intent)
                }

                // RecyclerView에 어댑터 연결
                recyclerView.adapter = adapter
            }
            .addOnFailureListener {
                Log.e("skrskr", "로그 불러오기 실패: ${it.message}")
            }
    }
}
