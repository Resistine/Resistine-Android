package com.resistine.android.ui.login

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object NetworkClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    fun sendOtp(email: String, callback: (Boolean, String?) -> Unit) {
        val json = JSONObject().put("email", email).toString()
        val body = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(SEND_OTP_URL)
            .addHeader("Accept", "application/json")
            .post(body)
            .build()

        Log.i(TAG, "Sending OTP request")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "OTP request failed before receiving an HTTP response", e)
                callback(false, "Could not reach the login service: ${e.message ?: "network error"}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = it.body?.string()
                    if (!it.isSuccessful) {
                        val errorDetails = buildHttpError(it.code, it.header(REQUEST_ID_HEADER), responseBody)
                        Log.e(TAG, "OTP request rejected: $errorDetails")
                        callback(false, errorDetails)
                        return@onResponse
                    }
                    Log.i(TAG, "OTP request accepted")
                    callback(true, null)
                }
            }
        })
    }

    fun verifyOtp(email: String, otp: String, publicKey: String, vpnName: String, callback: (Boolean, String?, String?) -> Unit) {
        val json = JSONObject()
            .put("email", email)
            .put("otp", otp)
            .put("client_public_key", publicKey)
            .put("vpn_name", vpnName)
            .put("host_url", "rfr1.resisti.net")
            .toString()

        val body = json.toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request.Builder()
            .url(VERIFY_OTP_URL)
            .addHeader("Accept", "application/json")
            .post(body)
            .build()

        Log.i(TAG, "Verifying OTP and requesting WireGuard configuration")
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "OTP verification failed before receiving an HTTP response", e)
                callback(false, "Could not reach the login service: ${e.message ?: "network error"}", null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val responseBody = it.body?.string()
                    if (!it.isSuccessful) {
                        val errorDetails = buildHttpError(it.code, it.header(REQUEST_ID_HEADER), responseBody)
                        Log.e(TAG, "OTP verification rejected: $errorDetails")
                        callback(false, errorDetails, null)
                        return@onResponse
                    }
                    
                    try {
                        if (responseBody != null) {
                            val jsonResponse = JSONObject(responseBody)
                            val status = jsonResponse.optString("status")
                            if (status == "success") {
                                val vpnConfig = jsonResponse.optString("vpn_config")
                                if (vpnConfig.isNotEmpty()) {
                                    Log.i(TAG, "OTP verified and WireGuard configuration received")
                                    callback(true, null, vpnConfig)
                                } else {
                                    Log.e(TAG, "OTP verification response did not include a WireGuard configuration")
                                    callback(false, "Failed to get configuration.", null)
                                }
                            } else {
                                val error = jsonResponse.optString("error", "Verification failed.")
                                Log.e(TAG, "OTP verification response reported failure")
                                callback(false, error, null)
                            }
                        } else {
                            Log.e(TAG, "OTP verification returned an empty response")
                            callback(false, "Empty response body", null)
                        }
                    } catch (ex: Exception) {
                        Log.e(TAG, "Could not parse OTP verification response", ex)
                        callback(false, ex.message, null)
                    }
                }
            }
        })
    }

    internal fun buildHttpError(statusCode: Int, requestId: String?, responseBody: String?): String {
        val detail = parseErrorDetail(responseBody)
        val requestSuffix = requestId?.takeIf { it.isNotBlank() }?.let { " Request ID: $it." }.orEmpty()
        return buildString {
            append("Login service returned HTTP ")
            append(statusCode)
            append('.')
            detail?.let {
                append(' ')
                append(it)
            }
            append(requestSuffix)
        }
    }

    private fun parseErrorDetail(responseBody: String?): String? {
        if (responseBody.isNullOrBlank()) return null
        return runCatching {
            val json = JSONObject(responseBody)
            when (val detail = json.opt("detail")) {
                is String -> detail
                is JSONArray -> (0 until detail.length())
                    .mapNotNull { index -> detail.optJSONObject(index)?.optString("msg")?.takeIf(String::isNotBlank) }
                    .joinToString("; ")
                    .takeIf(String::isNotBlank)
                else -> json.optString("error").takeIf(String::isNotBlank)
            }
        }.getOrNull()
    }

    private const val TAG = "LoginNetwork"
    private const val SEND_OTP_URL = "https://validate.resistine.work/send-otp"
    private const val VERIFY_OTP_URL = "https://validate.resistine.work/verify-otp"
    private const val REQUEST_ID_HEADER = "x-request-id"
}
