package com.example.nutriscanner.log

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nutriscanner.R
import com.example.nutriscanner.result.ResultActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MyLogActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MyLogAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mylog)

        recyclerView = findViewById(R.id.myLogRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchDataFromFirestore()
    }

    private fun fetchDataFromFirestore() {
        // 현재 로그인된 유저 UID 가져오기
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "로그인된 유저가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // users/{uid}/logs 컬렉션에서 모든 문서 조회
        db.collection("users")
            .document(userId)
            .collection("logs")
            .get()
            .addOnSuccessListener { result ->
                Log.d("MyLogActivity", "유저 로그 가져오기 성공: ${result.size()}개")

                // 문서 ID(id) 와 imageUri 필드를 LogItem 으로 매핑
                val logList = result.map { doc ->
                    LogItem(
                        id        = doc.id,
                        dateTime  = doc.id,
                        imageUri  = doc.getString("imageUri") ?: ""
                    )
                }
                    // 최신순 정렬
                    .sortedByDescending { it.dateTime }

                // 어댑터 세팅
                adapter = MyLogAdapter(logList.toMutableList()) { item ->
                    // 클릭 시 ResultActivity 로 uid, timestamp 전달
                    val intent = Intent(this, ResultActivity::class.java).apply {
                        putExtra("uid", userId)
                        putExtra("timestamp", item.id)
                    }
                    startActivity(intent)
                }
                recyclerView.adapter = adapter
            }
            .addOnFailureListener { e ->
                Log.e("MyLogActivity", "로그 불러오기 실패", e)
                Toast.makeText(this, "로그를 불러올 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
    }
}
