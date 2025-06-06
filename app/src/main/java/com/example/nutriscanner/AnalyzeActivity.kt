package com.example.nutriscanner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class AnalyzeActivity : AppCompatActivity() {

    // 권한 요청 코드
    private val REQUEST_STORAGE_PERMISSION = 100
    private val GALLERY_INTENT_CODE = 101

    // 뷰 참조
    private lateinit var photoCard: CardView
    private lateinit var placeholderLayout: LinearLayout
    private lateinit var selectedPhoto: ImageView
    private lateinit var analyzeImageButton: Button

    private lateinit var addFoodButton: ImageButton
    private lateinit var foodListContainer: LinearLayout
    private lateinit var analyzeNutritionButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyze)

        val appNameText = findViewById<TextView>(R.id.appName)

        appNameText.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
        }

        // 1) 레이아웃 내 뷰들 바인딩
        photoCard            = findViewById(R.id.photoCard)
        placeholderLayout    = findViewById(R.id.placeholderLayout)
        selectedPhoto        = findViewById(R.id.selectedPhoto)
        analyzeImageButton   = findViewById(R.id.analyzeImageButton)

        addFoodButton        = findViewById(R.id.addFoodButton)
        foodListContainer    = findViewById(R.id.foodListContainer)
        analyzeNutritionButton = findViewById(R.id.analyzeNutritionButton)

        // 2) 초기 상태
        selectedPhoto.visibility     = View.GONE
        analyzeImageButton.isEnabled = false   // 사진 선택 전엔 비활성화해둠

        // 3) photoCard(카드 전체) 클릭 → 갤러리 열기
        photoCard.setOnClickListener {
            openGalleryWithPermissionCheck()
        }

        // 4) 이미지 분석하기 버튼 클릭 시 (예시: 토스트 출력)
        analyzeImageButton.setOnClickListener {
            Toast.makeText(this, "이미지 분석 로직을 여기에 추가하세요.", Toast.LENGTH_SHORT).show()
        }

        // 5) 음식목록 +버튼 클릭 → 다이얼로그 띄우기
        addFoodButton.setOnClickListener {
            showAddFoodDialog()
        }

        // 6) 영양성분 분석하기 버튼 클릭 (예시)
        analyzeNutritionButton.setOnClickListener {
            Toast.makeText(this, "영양성분 분석 로직을 여기에 추가하세요.", Toast.LENGTH_SHORT).show()
        }
    }

    // 권한이 있으면 갤러리 실행, 없으면 요청
    private fun openGalleryWithPermissionCheck() {
        if (checkStoragePermission()) {
            openGallery()
        } else {
            requestStoragePermission()
        }
    }

    // Android 버전에 맞춰 읽기 권한 확인
    private fun checkStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_MEDIA_IMAGES
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    // 권한 요청
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

    // 갤러리 열기
    private fun openGallery() {
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        galleryIntent.type = "image/*"
        startActivityForResult(galleryIntent, GALLERY_INTENT_CODE)
    }

    // 권한 요청 결과 수신
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "저장소 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 갤러리에서 선택된 이미지 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_INTENT_CODE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            if (imageUri != null) {
                // 1) 안내 레이아웃 숨기고
                placeholderLayout.visibility = View.GONE

                // 2) 선택된 이미지 보이게 설정
                selectedPhoto.visibility = View.VISIBLE
                selectedPhoto.setImageURI(imageUri)

                // 3) 이미지 분석 버튼 활성화
                analyzeImageButton.isEnabled = true
            }
        }
    }

    // 음식 추가 다이얼로그 띄우기
    private fun showAddFoodDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("음식 추가")

        val editText = EditText(this)
        editText.hint = "음식 이름 입력"
        editText.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(editText)

        builder.setPositiveButton("추가") { dialog, _ ->
            val foodName = editText.text.toString().trim()
            if (foodName.isNotEmpty()) {
                addFoodItem(foodName)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    // 동적으로 음식 목록 아이템 추가
    private fun addFoodItem(name: String) {
        // item_food.xml 레이아웃을 인플레이트
        val inflater = LayoutInflater.from(this)
        val itemView = inflater.inflate(R.layout.item_food, foodListContainer, false)

        // 인플레이트된 뷰에서 텍스트와 삭제 버튼 찾기
        val tvFoodName = itemView.findViewById<TextView>(R.id.foodNameText)
        val btnRemove  = itemView.findViewById<ImageButton>(R.id.removeFoodButton)

        tvFoodName.text = name

        // 삭제 버튼 클릭 시, 이 뷰를 컨테이너에서 제거
        btnRemove.setOnClickListener {
            foodListContainer.removeView(itemView)
        }

        // 컨테이너에 아이템 뷰 추가
        foodListContainer.addView(itemView)
    }
}
