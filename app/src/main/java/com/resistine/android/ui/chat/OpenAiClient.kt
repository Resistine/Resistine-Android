package com.resistine.android.ui.chat

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object OpenAiClient {

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    fun getChatResponse(userText: String, callback: (String) -> Unit) {
//        val url = "http://10.49.64.53:8001/v1/chat/completions"
        val url = " http://dashboard.resisti.net:8001/v1"
        val jsonBody = JSONObject()
        jsonBody.put("model", "ministral-3:14b")
        val messagesArray = JSONObject().apply {
            put("role", "user")
            put("content", userText)
        }
        jsonBody.put("messages", org.json.JSONArray(listOf(messagesArray)))

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Error: Could not get response from AI. Details: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    try {
                        val jsonObject = JSONObject(responseBody!!)
                        val choices = jsonObject.getJSONArray("choices")
                        val firstChoice = choices.getJSONObject(0)
                        val message = firstChoice.getJSONObject("message")
                        val content = message.getString("content")
                        callback(content.trim())
                    } catch (e: Exception) {
                        callback("Error: Could not parse AI response.")
                    }
                } else {
                    callback("Error: API request failed with code ${response.code} and message: ${response.message}")
                }
            }
        })
    }
}
