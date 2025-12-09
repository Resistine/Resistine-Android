package com.resistine.android.ui.chat

import com.resistine.android.BuildConfig
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

object OpenAiClient {

    private val client = OkHttpClient()

    fun getChatResponse(userText: String, callback: (String) -> Unit) {
        val apiKey = BuildConfig.OPENAI_API_KEY
        val url = "https://api.openai.com/v1/chat/completions"

        if (apiKey.isBlank()) {
            callback("Error: OpenAI API key not set. Please set it in your local.properties file.")
            return
        }

        val jsonBody = JSONObject()
        jsonBody.put("model", "gpt-3.5-turbo")
        val messagesArray = JSONObject().apply {
            put("role", "user")
            put("content", userText)
        }
        jsonBody.put("messages", org.json.JSONArray(listOf(messagesArray)))

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(jsonBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Error: Could not get response from AI. Please try again later.")
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
                    callback("Error: API request failed with code ${response.code}")
                }
            }
        })
    }
}
