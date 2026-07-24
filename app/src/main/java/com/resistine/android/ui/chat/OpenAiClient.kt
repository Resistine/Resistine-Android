package com.resistine.android.ui.chat

import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object OpenAiClient {
    private const val URL = "http://10.49.64.53:8001/v1/chat/completions"
    private const val MODEL = "ministral-3:14b"
    private const val MAX_CONTEXT_MESSAGES = 20

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    fun getChatResponse(
        history: List<ChatMessage>,
        callback: (Result<String>) -> Unit
    ): Call {
        val messages = JSONArray().apply {
            put(
                JSONObject()
                    .put("role", "system")
                    .put(
                        "content",
                        "You are Resistine, an Android security assistant. Be concise, explain risk " +
                            "without exaggeration, use Markdown when structure helps, and never claim " +
                            "to have inspected device data that was not included in the conversation."
                    )
            )
            history.takeLast(MAX_CONTEXT_MESSAGES).forEach { message ->
                put(
                    JSONObject()
                        .put("role", if (message.isUser) "user" else "assistant")
                        .put("content", message.text)
                )
            }
        }
        val body = JSONObject()
            .put("model", MODEL)
            .put("messages", messages)
            .toString()
            .toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(URL)
            .header("Content-Type", "application/json")
            .post(body)
            .build()

        return client.newCall(request).also { call ->
            call.enqueue(object : Callback {
                override fun onFailure(call: Call, error: IOException) {
                    if (!call.isCanceled()) {
                        callback(Result.failure(error))
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        if (!response.isSuccessful) {
                            callback(Result.failure(IOException("Assistant returned HTTP ${response.code}.")))
                            return
                        }
                        val responseBody = response.body?.string()
                        val content = runCatching {
                            JSONObject(responseBody.orEmpty())
                                .getJSONArray("choices")
                                .getJSONObject(0)
                                .getJSONObject("message")
                                .getString("content")
                                .trim()
                                .takeIf(String::isNotEmpty)
                                ?: error("Assistant returned an empty response.")
                        }
                        callback(content)
                    }
                }
            })
        }
    }
}
