package com.example.mathsolver.domain

import android.util.Base64
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import org.json.JSONObject

class BackendApi(private val userId: String) {
    private val client = OkHttpClient()
    fun solveFromImage(
        imageBytes: ByteArray,
        onResult: (String, String) -> Unit,
    ) {
        val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)
        val json = """
            {
                "image_base64": "$base64",
                "user_id": "$userId"
            }
        """.trimIndent()
        val request = Request.Builder()
            .url("http://10.19.14.22:8000/solve-from-image")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                onResult("Error: ${e.message}", "")
            }
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                if (body != null) {
                    try {
                        val json = org.json.JSONObject(body)

                        val equation = json.optString("equation", "")
                        val solution = json.optString("solution", "")

                        onResult(equation, solution)

                    } catch (e: Exception) {
                        onResult("Parse error", "")
                    }
                } else {
                    onResult("Empty response", "")
                }
            }
        })
    }

    fun solveFromText(
        equation: String,
        onResult: (String) -> Unit
    ) {
        val safeEquation = JSONObject.quote(equation.trim())

        val json = """
        {
            "equation": $safeEquation,
            "user_id": "$userId"
        }
        """

        val request = Request.Builder()
            .url("http://10.19.14.22:8000/solve")
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                if (body != null) {
                    try {
                        val json = JSONObject(body)

                        val equation = json.optString("equation", "")
                        val solution = json.optString("solution", "")

                        onResult(solution) // или можно вернуть оба

                    } catch (e: Exception) {
                        onResult("Parse error")
                    }
                } else {
                    onResult("Empty response")
                }
            }
        })
    }

    fun getHistory(onResult: (String) -> Unit) {
        val request = Request.Builder()
            .url("http://10.19.14.22:8000/history?user_id=$userId")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()

                if (body != null) {
                    try {
                        val jsonArray = org.json.JSONArray(body)

                        val result = StringBuilder()

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val eq = item.optString("equation")
                            val sol = item.optString("solution")
                            val time = item.optString("created_at")

                            result.append("$eq → $sol\n")
                        }

                        onResult(result.toString())

                    } catch (e: Exception) {
                        onResult("Parse error")
                    }
                } else {
                    onResult("Empty response")
                }
            }
        })
    }
}