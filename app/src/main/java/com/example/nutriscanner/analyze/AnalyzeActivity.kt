package com.example.nutriscanner.analyze

import com.example.nutriscanner.BuildConfig
import com.example.nutriscanner.api.ApiClient
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.example.nutriscanner.MainActivity
import com.example.nutriscanner.R
import androidx.lifecycle.lifecycleScope
import com.example.nutriscanner.result.ResultActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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

    // 이미지 분석용 변수
    private lateinit var foodAnalyzer: FoodAnalyzer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analyze)

        // FoodAnalyzer 인스턴스 초기화
        foodAnalyzer = FoodAnalyzer(this)

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

        // 클릭시 홈화면으로
        appName.setOnClickListener {
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

        // MainActivity에서 넘어온 Intent에 이미지 정보가 있는지 확인
        handleIncomingImage()

        // 3) photoCard(카드 전체) 클릭 → 갤러리 열기
        photoCard.setOnClickListener {
            openGalleryWithPermissionCheck()
        }

        // 4) 이미지 분석하기 버튼 클릭 시
        analyzeImageButton.setOnClickListener {
            selectedPhoto.drawable?.let { drawable ->
                val bitmap = (drawable as BitmapDrawable).bitmap
                val foodName = foodAnalyzer.analyzeImage(bitmap) // 이미지 분석

                // 분석된 음식 리스트에 추가
                addFoodItem(foodName)
            }
        }

        // 5) 음식목록 +버튼 클릭 -> 다이얼로그 띄우기
        addFoodButton.setOnClickListener {
            showAddFoodDialog()
        }

        // 6) 영양성분 분석하기 버튼 클릭
        analyzeNutritionButton.setOnClickListener {
            // 0) 버튼 클릭 바로 로그
            Log.d("AnalyzeActivity", "분석 버튼 클릭됨")

            // 1) 음식 이름 리스트 준비
            val foodNames = foodListContainer.children
                .map { (it as LinearLayout)
                    .findViewById<TextView>(R.id.foodNameText)
                    .text.toString() }
                .toList()
            Log.d("AnalyzeActivity", "화면에서 가져온 foodNames = $foodNames")

            // 2) Coroutine 시작
            lifecycleScope.launch {
                try {
                    Log.d("AnalyzeActivity", "코루틴 시작, API 호출 준비")

                    // 2-1) 식약처 API로 영양성분 정보 가져오기
                    val foodData = mutableListOf<Map<String, Any>>()
                    for (name in foodNames) {
                        Log.d("AnalyzeActivity", "API 요청: $name")
                        val resp = ApiClient.service.getFoods(
                            serviceKey = BuildConfig.FOOD_API_KEY,
                            foodName   = name
                        )

                        if (resp.isSuccessful) {
                            val item = resp.body()?.body?.items?.firstOrNull()
                            if (item != null) {
                                Log.d("AnalyzeActivity", "API 응답 성공: $item")

                                // 1) “100g” 처럼 붙어오는 단위에서 숫자만 뽑아내고
                                val baseSize = item.servingSize
                                    .replace(Regex("[^0-9.]"), "")
                                    .toDoubleOrNull() ?: 100.0

                                // 2) “230.000g” 처럼 붙어오는 실제 제공량에서 숫자만 뽑아
                                val actualSize = (item.actualServing ?: "${baseSize}")
                                    .replace(Regex("[^0-9.]"), "")
                                    .toDoubleOrNull() ?: baseSize

                                // 3) ratio 계산
                                val ratio = actualSize / baseSize


                                foodData += mapOf(
                                    "name" to item.name,
                                    "nutrients" to mapOf(
                                        "탄수화물" to item.carbs * ratio,
                                        "단백질"   to item.protein * ratio,
                                        "지방"     to item.fat * ratio,
                                        "나트륨"   to item.sodium * ratio,
                                        "당류"     to item.sugar * ratio
                                    ),
                                    "servingSize"    to item.servingSize,
                                    "actualServing"  to (item.actualServing ?: "" )
                                )
                            } else {
                                Log.w("AnalyzeActivity", "아이템이 없어요 for $name")
                            }
                        } else {
                            Log.e("AnalyzeActivity", "API 에러: code=${resp.code()}, body=${resp.errorBody()?.string()}")
                        }
                    }

                    Log.d("AnalyzeActivity", "모은 foodData = $foodData")

                    // 2-2) GPT 분석 요청
                    Log.d("AnalyzeActivity", "GPT 호출 전, foodData size=${foodData.size}")
                    val nutritionAnalyzer = NutritionAnalyzer(this@AnalyzeActivity)
                    nutritionAnalyzer.getNutritionFeedback(foodData) { feedback ->
                        Log.d("AnalyzeActivity", "GPT 응답 feedback = $feedback")

                        // 1) 사진 업로드 후 URL 받기
                        uploadImageAndGetUrl { imageUrl ->
                            // 2) 모든 음식의 영양성분 합계 계산
                            val totalNutrients = calcTotalNutrients(foodData)

                            // 3) Firestore 에 저장할 데이터 준비
                            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@uploadImageAndGetUrl
                            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).apply {
                                timeZone = TimeZone.getTimeZone("Asia/Seoul")
                            }
                            val timestamp = sdf.format(Date())

                            val logDoc = mapOf(
                                "food"           to foodData,
                                "gptComment"     to feedback,
                                "imageUri"       to imageUrl,
                                "totalNutrients" to totalNutrients,
                                "timestamp"      to timestamp
                            )
                            Log.d("AnalyzeActivity", "Firestore에 저장할 logDoc = $logDoc")

                            // 4) Firestore 저장
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .collection("logs")
                                .document(timestamp)
                                .set(logDoc)
                                .addOnSuccessListener {
                                    Log.d("AnalyzeActivity", "Firestore 저장 성공")
                                    Toast.makeText(this@AnalyzeActivity, "저장 성공!", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(this@AnalyzeActivity, ResultActivity::class.java).apply {
                                        putExtra("uid", uid)
                                        putExtra("timestamp", timestamp)
                                    }
                                    startActivity(intent)
                                }
                                .addOnFailureListener { e ->
                                    Log.e("AnalyzeActivity", "Firestore 저장 실패", e)
                                    Toast.makeText(this@AnalyzeActivity, "저장 실패…", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                } catch (e: Exception) {
                    Log.e("AnalyzeActivity", "코루틴 예외 발생", e)
                    Toast.makeText(this@AnalyzeActivity, "오류 발생: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }



    }



    private fun handleIncomingImage() {
        // Intent에 "imageBitmap" 있으면 Bitmap으로 처리
        val bmp = intent.getParcelableExtra<Bitmap>("imageBitmap")
        if (bmp != null) {
            displayImage(bmp)
            return
        }

        // Uri로 넘겼다면 "imageUri"로 처리
        val uriString = intent.getStringExtra("imageUri")
        if (!uriString.isNullOrEmpty()) {
            val uri = Uri.parse(uriString)
            displayImage(uri)
        }
    }

    // Bitmap 형태로 들어왔을 때 보여주는 함수
    private fun displayImage(bitmap: Bitmap) {
        placeholderLayout.visibility = View.GONE
        selectedPhoto.visibility     = View.VISIBLE
        selectedPhoto.setImageBitmap(bitmap)
        analyzeImageButton.isEnabled = true
    }

    // Uri 형태로 들어왔을 때 보여주는 함수
    private fun displayImage(uri: Uri) {
        placeholderLayout.visibility = View.GONE
        selectedPhoto.visibility     = View.VISIBLE
        selectedPhoto.setImageURI(uri)
        analyzeImageButton.isEnabled = true
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

    private fun uploadImageAndGetUrl(onResult: (String) -> Unit) {
        // 타임스탬프 기반으로 유니크한 경로
        val filename = "images/${System.currentTimeMillis()}.png"
        val storageRef = FirebaseStorage.getInstance().reference.child(filename)

        // Bitmap -> ByteArray
        val drawable = selectedPhoto.drawable as? BitmapDrawable
        drawable?.bitmap?.let { bmp ->
            ByteArrayOutputStream().use { baos ->
                bmp.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val data = baos.toByteArray()
                storageRef.putBytes(data)
                    .addOnSuccessListener { _ ->
                        storageRef.downloadUrl
                            .addOnSuccessListener { uri ->
                                onResult(uri.toString())
                            }
                            .addOnFailureListener { e ->
                                Log.e("AnalyzeActivity", "다운로드 URL 획득 실패", e)
                                onResult("")  // 실패 시 빈 문자열
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("AnalyzeActivity", "이미지 업로드 실패", e)
                        onResult("")
                    }
            }
        } ?: run {
            onResult("")  // drawable 이 없으면 빈 문자열
        }
    }

    private fun calcTotalNutrients(foodData: List<Map<String, Any>>): Map<String, Double> {
        var totalCarbs = 0.0
        var totalProtein = 0.0
        var totalFat = 0.0
        var totalSodium = 0.0
        var totalSugar = 0.0

        for (food in foodData) {
            val nutrients = food["nutrients"] as? Map<*, *>
            totalCarbs   += (nutrients?.get("탄수화물") as? Number)?.toDouble() ?: 0.0
            totalProtein+= (nutrients?.get("단백질")   as? Number)?.toDouble() ?: 0.0
            totalFat     += (nutrients?.get("지방")     as? Number)?.toDouble() ?: 0.0
            totalSodium  += (nutrients?.get("나트륨")   as? Number)?.toDouble() ?: 0.0
            totalSugar   += (nutrients?.get("당류")     as? Number)?.toDouble() ?: 0.0
        }

        return mapOf(
            "탄수화물" to totalCarbs,
            "단백질"   to totalProtein,
            "지방"     to totalFat,
            "나트륨"   to totalSodium,
            "당류"     to totalSugar
        )
    }
}
