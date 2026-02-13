package com.resistine.android.ui.login

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object NetworkClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .build()

    fun sendOtp(email: String, callback: (Boolean, String?) -> Unit) {
        val url = "https://validate.resistine.work/send-otp"
        val json = JSONObject().put("email", email).toString()
        val body = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        val errorDetails = "HTTP ${it.code}. Body: ${it.body?.string()}"
                        callback(false, errorDetails)
                        return@onResponse
                    }
                    callback(true, null)
                }
            }
        })
    }

    fun verifyOtp(email: String, otp: String, publicKey: String, vpnName: String, callback: (Boolean, String?, String?) -> Unit) {
        val url = "https://validate.resistine.work/verify-otp"
        val json = JSONObject()
            .put("email", email)
            .put("otp", otp)
            .put("client_public_key", publicKey)
            .put("vpn_name", vpnName)
            .toString()

        val body = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, e.message, null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = it.body?.string()
                    if (!it.isSuccessful) {
                        val errorDetails = "HTTP ${it.code}. Body: $responseBody"
                        callback(false, errorDetails, null)
                        return@onResponse
                    }
                    
                    try {
                        if (responseBody != null) {
                            val jsonResponse = JSONObject(responseBody)
                            val status = jsonResponse.optString("status")
                            if (status == "verified") {
                                val vpnConfig = jsonResponse.optString("vpn_config")
                                if (vpnConfig.isNotEmpty()) {
                                    callback(true, null, vpnConfig)
                                } else {
                                    callback(false, "Failed to get configuration.", null)
                                }
                            } else {
                                val error = jsonResponse.optString("error", "Verification failed.")
                                callback(false, error, null)
                            }
                        } else {
                            callback(false, "Empty response body", null)
                        }
                    } catch (ex: Exception) {
                        callback(false, ex.message, null)
                    }
                }
            }
        })
    }
}
