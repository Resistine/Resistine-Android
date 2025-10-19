package com.resistine.android

import okhttp3.*
import java.io.IOException

object NetworkUtils {
    fun fetchPublicIp(callback: (String) -> Unit) {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.ipify.org?format=text")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let {
                    callback(it.trim())
                } ?: callback("No response")
            }
        })
    }
}
