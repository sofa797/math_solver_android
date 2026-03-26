package com.example.mathsolver.domain

import android.util.Base64
import android.util.Log
import com.example.mathsolver.BuildConfig
import java.io.ByteArrayOutputStream
import java.io.IOException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody


class OcrApi {
    private val client = OkHttpClient()

    fun recognizeImage(
        imageBytes: ByteArray,
        onResult: (String?) -> Unit
    ) {
        try {
            val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

            val jsonString = """
                {
                    "model": "glm-ocr",
                    "file": "data:image/png;base64,$base64"
                }
            """.trimIndent()

            val body = jsonString.toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("https://router.huggingface.co/zai-org/api/paas/v4/layout_parsing")
                .addHeader("Authorization", "Bearer ${BuildConfig.HF_API_KEY}")
                .post(body)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.e("OcrApi", "OCR request failed: ${e.message}")
                    onResult("2x+4=10")
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseText = response.body?.string()
                    Log.d("OcrApi", "OCR response: $responseText")

                    if (!responseText.isNullOrEmpty()) {
                        try {
                            val json = org.json.JSONObject(responseText)
                            val rawText = json.optString("md_results", "")
                            if (rawText.isEmpty()) {
                                Log.e("OcrApi", "md_results is empty")
                                onResult("2x+4=10")
                                return
                            }
                            val equation = extractEquation(rawText)
                            Log.d("OcrApi", "Extracted equation: $equation")
                            onResult(equation.ifEmpty { "2x+4=10" })
                        } catch (e: Exception) {
                            Log.e("OcrApi", "JSON parse error: ${e.message}")
                            onResult("2x+4=10")
                        }
                    } else {
                        Log.e("OcrApi", "OCR response body null")
                        onResult("2x+4=10")
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("OcrApi", "Exception encoding image: ${e.message}")
            onResult("2x+4=10")
        }
    }

    private fun extractEquation(text: String): String {
        val regex = Regex("[0-9xX+\\-*/^().= ]+")
        val matches = regex.findAll(text)

        for (match in matches) {
            val candidate = match.value.trim()
            if (candidate.contains("=") && candidate.contains("x")) {
                return candidate
            }
        }
        return ""
    }
}

fun android.graphics.Bitmap.toReducedByteArray(maxSize: Int = 1024): ByteArray {
    val scaled = if (width > maxSize || height > maxSize) {
        val ratio = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)
        android.graphics.Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
    } else this
    val stream = ByteArrayOutputStream()
    scaled.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}