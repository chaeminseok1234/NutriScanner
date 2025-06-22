package com.example.nutriscanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.nutriscanner.analyze.AnalyzeActivity
import com.example.nutriscanner.log.MyLogActivity
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private lateinit var cameraButton: ImageButton
    private lateinit var galleryButton: ImageButton
    private lateinit var recordButton: Button

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menuButton: ImageButton

    private val REQUEST_CAMERA_PERMISSION = 1
    private val REQUEST_STORAGE_PERMISSION = 2

    private val CAMERA_INTENT_CODE = 3
    private val GALLERY_INTENT_CODE = 4

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

        initViews()
        setupButtonListeners()
        setupDrawerMenu()

        // 1) XML에 정의해둔 layoutAnimation 리소스 불러오기
        val controller = AnimationUtils.loadLayoutAnimation(this, R.anim.layout_fade)

        // 2) greetingContainer는 <LinearLayout android:id="@+id/greetingContainer"> 여기에 추가
        val container = findViewById<LinearLayout>(R.id.greetingContainer)
        container.layoutAnimation = controller

        // 3) string-array 읽어서 한 줄씩 TextView로 만들어 추가
        val lines = resources.getStringArray(R.array.greeting_lines)
        for (line in lines) {
            val tv = TextView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                textSize = 24f
                setTextColor(Color.BLACK)
            }

            // 4) Spannable 처리
            val ssb = SpannableString(line)

            // 4-1) "AI" 볼드
            val aiIndex = line.indexOf("AI")
            if (aiIndex >= 0) {
                ssb.setSpan(
                    StyleSpan(Typeface.BOLD),
                    aiIndex, aiIndex + 2,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            // 4-2) "NutriScanner" 의 N 색깔 #42A5F5, S 색깔 #26A69A
            val nutriIndex = line.indexOf("NutriScanner")
            if (nutriIndex >= 0) {
                // 'N'
                ssb.setSpan(
                    ForegroundColorSpan(Color.parseColor("#42A5F5")),
                    nutriIndex, nutriIndex + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                // 'S' (문자열의 다섯 번째 문자)
                val sPos = nutriIndex + 5
                ssb.setSpan(
                    ForegroundColorSpan(Color.parseColor("#26A69A")),
                    sPos, sPos + 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }

            // 5) 최종 텍스트 설정
            tv.text = ssb

            // 6) 컨테이너에 추가
            container.addView(tv)
        }

        // 7) 애니메이션 시작
        container.startLayoutAnimation()

        // 헤더 색깔 조정
        val appName = findViewById<TextView>(R.id.appName)
        val text = "NutriScanner"
        val spannable = SpannableString(text).apply {
            // 0번 인덱스부터 1글자(N) → 파란색
            setSpan(
                ForegroundColorSpan(Color.parseColor("#42A5F5")),
                0, 1,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            // 3번 인덱스부터 4글자 중 하나(S) → 초록색
            setSpan(
                ForegroundColorSpan(Color.parseColor("#26A69A")),
                5, 6,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        appName.text = spannable

    }

    private fun checkCameraPermission(): Boolean {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
        return permission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }


    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_STORAGE_PERMISSION
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(cameraIntent, CAMERA_INTENT_CODE)
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, GALLERY_INTENT_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                } else {
                    Toast.makeText(this, "카메라 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                }
            }
            REQUEST_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    Toast.makeText(this, "저장소 권한이 필요합니다", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return

        when (requestCode) {
            CAMERA_INTENT_CODE -> {
                // 카메라에서 찍은 썸네일 Bitmap 받아오기
                val photoBitmap = data?.extras?.get("data") as? Bitmap
                if (photoBitmap != null) {
                    // AnalyzeActivity로 Bitmap 넘겨주기
                    val intent = Intent(this, AnalyzeActivity::class.java)
                    intent.putExtra("imageBitmap", photoBitmap)
                    startActivity(intent)
                }
            }
            GALLERY_INTENT_CODE -> {
                val imageUri: Uri? = data?.data
                if (imageUri != null) {
                    // 선택한 이미지 Uri 처리
                    Toast.makeText(this, "사진 선택 완료", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initViews() {
        cameraButton = findViewById(R.id.cameraButton)
        galleryButton = findViewById(R.id.galleryButton)
        recordButton = findViewById(R.id.recordButton)
        drawerLayout = findViewById(R.id.drawerLayout)
        menuButton = findViewById(R.id.menuButton)
    }

    private fun setupButtonListeners() {
        cameraButton.setOnClickListener {
            if (checkCameraPermission()) {
                openCamera()
            } else {
                requestCameraPermission()
            }
        }

        galleryButton.setOnClickListener {
            val intent = Intent(this, AnalyzeActivity::class.java)
            startActivity(intent)
        }

        recordButton.setOnClickListener {
            startActivity(Intent(this, MyLogActivity::class.java))
        }

        menuButton.setOnClickListener {
            if (!drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.openDrawer(GravityCompat.START)
            }
        }
    }

    private fun setupDrawerMenu() {
        val navView = findViewById<NavigationView>(R.id.navigationView)
        val menu    = navView.menu

        // 현재 로그인된 유저 정보 가져오기
        val user = FirebaseAuth.getInstance().currentUser
        // displayName 이 null 이라면, email 앞부분을 대신 사용
        val nameToShow = user?.displayName
            ?: user?.email?.substringBefore("@")
            ?: "Guest"

        // nav_user_name 아이템의 타이틀을 실제 이름으로 변경
        menu.findItem(R.id.nav_user_name).title = nameToShow

        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_camera -> {
                    if (checkCameraPermission()) openCamera() else requestCameraPermission()
                }
                R.id.nav_gallery -> {
                    if (checkStoragePermission()) openGallery() else requestStoragePermission()
                }
                R.id.nav_my_logs -> {
                    startActivity(Intent(this, MyLogActivity::class.java))
                }
                R.id.nav_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(
                        Intent(this, LoginActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                else -> return@setNavigationItemSelectedListener false
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }


}
