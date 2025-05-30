package com.example.nutriscanner

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1) Auth 인스턴스 가져와서
        auth = FirebaseAuth.getInstance()

        // 2) 로그인 안 된 상태면 LoginActivity로
        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // 3) 로그인 되어 있으면 레이아웃 세팅
        setContentView(R.layout.activity_main)

        // 4) 인사 텍스트 업데이트
        val name = auth.currentUser?.displayName ?: "User"

    }
}