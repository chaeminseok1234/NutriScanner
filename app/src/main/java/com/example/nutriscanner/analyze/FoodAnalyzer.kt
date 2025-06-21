package com.example.nutriscanner.analyze

import android.content.Context
import android.graphics.Bitmap
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FoodAnalyzer(context: Context) {

    private var interpreter: Interpreter
    private var foodList: MutableList<String> = mutableListOf()

    init {
        // assets 폴더에서 음식 머신러닝 모델 불러옴
        val model = context.assets.open("food_model.tflite") // "food_model.tflite" 파일을 assets에서 읽음
        val modelFile = File(context.cacheDir, "food_model.tflite") // 임시 파일 생성

        // 모델 파일을 File로 복사
        model.copyTo(modelFile.outputStream())

        // TensorFlow Lite 인터프리터 생성
        interpreter = Interpreter(modelFile)

        loadFoodList(context)
    }

    // 이미지를 모델에 맞게 변환하고 분석
    fun analyzeImage(bitmap: Bitmap): String {
        val imageBuffer = preprocessImage(bitmap)  // 이미지 전처리

        // 출력 크기를 [1, 150]으로 설정, 텐서플로우 파일이 음식 150개로 학습된 데이터
        val output = Array(1) { FloatArray(150) }  // 예시로 150개의 음식 카테고리 예측
        interpreter.run(imageBuffer, output)  // 모델 예측



        // 예측된 인덱스
        val predictedCategoryIndex = output[0].indices.maxByOrNull { output[0][it] } ?: 0
        return foodList[predictedCategoryIndex]  // 예측된 음식 반환
    }

    // 이미지를 모델 입력에 맞게 전처리
    private fun preprocessImage(bitmap: Bitmap): ByteBuffer {
        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val buffer = ByteBuffer.allocateDirect(4 * 224 * 224 * 3)  // 224x224 크기, RGB 채널
        buffer.order(ByteOrder.nativeOrder())

        // 이미지를 ByteBuffer에 넣기
        for (y in 0 until 224) {
            for (x in 0 until 224) {
                val pixel = resizedBitmap.getPixel(x, y)
                buffer.putFloat((pixel shr 16 and 0xFF) / 255.0f)  // Red
                buffer.putFloat((pixel shr 8 and 0xFF) / 255.0f)   // Green
                buffer.putFloat((pixel and 0xFF) / 255.0f)        // Blue
            }
        }

        return buffer
    }

    // 예측 결과를 분석하여 가장 높은 확률을 가진 음식 카테고리 출력
    private fun loadFoodList(context: Context) {
        val inputStream: InputStream = context.assets.open("food_list.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonString)
        val foodArray = jsonObject.getJSONArray("food_list")

        foodList = mutableListOf<String>()
        for (i in 0 until foodArray.length()) {
            foodList.add(foodArray.getString(i))
        }
    }

}
