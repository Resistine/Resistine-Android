package com.resistine.android.ui.integrity

import android.app.Activity
import android.content.Context
import android.util.Base64
import com.google.android.gms.tasks.Task
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.StandardIntegrityManager
import com.resistine.android.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PlayIntegrityRepository(
    context: Context,
    private val cloudProjectNumber: Long = BuildConfig.PLAY_INTEGRITY_CLOUD_PROJECT_NUMBER,
    private val verificationUrl: String = BuildConfig.PLAY_INTEGRITY_VERIFICATION_URL
) {
    private val appContext = context.applicationContext
    private val integrityManager = IntegrityManagerFactory.createStandard(appContext)
    private val providerMutex = Mutex()
    private var tokenProvider: StandardIntegrityManager.StandardIntegrityTokenProvider? = null
    private var lastToken: StandardIntegrityManager.StandardIntegrityToken? = null
    private var lastRemediationType: Int? = null
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val isConfigured: Boolean
        get() = cloudProjectNumber > 0L && verificationUrl.startsWith("https://")

    fun initialState(): IntegrityUiState {
        return if (isConfigured) {
            IntegrityUiState()
        } else {
            IntegrityUiState(
                status = IntegrityCheckStatus.CONFIGURATION_REQUIRED,
                message = "Play Integrity needs a linked Cloud project and an HTTPS verification endpoint. App inventory scanning remains fully available on-device."
            )
        }
    }

    suspend fun check(): IntegrityUiState {
        if (!isConfigured) return initialState()
        lastToken = null
        lastRemediationType = null
        return try {
            val action = createAction()
            val provider = getOrPrepareProvider()
            val token = provider.request(
                StandardIntegrityManager.StandardIntegrityTokenRequest.builder()
                    .setRequestHash(action.requestHash)
                    .build()
            ).awaitResult()
            lastToken = token
            verifyOnServer(token.token(), action)
        } catch (error: Throwable) {
            tokenProvider = null
            IntegrityUiState(
                status = IntegrityCheckStatus.ERROR,
                message = error.toUserMessage()
            )
        }
    }

    suspend fun showRemediation(activity: Activity): Int {
        val token = lastToken ?: error("Run the environment check again before remediation.")
        val type = lastRemediationType ?: error("The verifier did not request remediation.")
        val response = StandardIntegrityManager.StandardIntegrityDialogRequest
            .StandardIntegrityResponse.TokenResponse(token)
        val request = StandardIntegrityManager.StandardIntegrityDialogRequest.builder()
            .setActivity(activity)
            .setStandardIntegrityResponse(response)
            .setTypeCode(type)
            .build()
        return integrityManager.showDialog(request).awaitResult()
    }

    private suspend fun getOrPrepareProvider(): StandardIntegrityManager.StandardIntegrityTokenProvider {
        tokenProvider?.let { return it }
        return providerMutex.withLock {
            tokenProvider ?: integrityManager.prepareIntegrityToken(
                StandardIntegrityManager.PrepareIntegrityTokenRequest.builder()
                    .setCloudProjectNumber(cloudProjectNumber)
                    .build()
            ).awaitResult().also { tokenProvider = it }
        }
    }

    private fun createAction(now: Long = System.currentTimeMillis()): IntegrityAction {
        val actionId = UUID.randomUUID().toString()
        val canonical = canonicalAction(appContext.packageName, actionId, now)
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(canonical.toByteArray(Charsets.UTF_8))
        val requestHash = Base64.encodeToString(
            digest,
            Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        )
        return IntegrityAction(actionId, now, requestHash)
    }

    private suspend fun verifyOnServer(
        token: String,
        action: IntegrityAction
    ): IntegrityUiState = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("token", token)
            .put("packageName", appContext.packageName)
            .put("actionId", action.actionId)
            .put("timestampMillis", action.timestampMillis)
            .put("requestHash", action.requestHash)
            .toString()
            .toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(verificationUrl)
            .post(body)
            .header("Accept", "application/json")
            .build()

        httpClient.newCall(request).execute().use { response ->
            val responseText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val detail = runCatching {
                    JSONObject(responseText).optString("message")
                }.getOrNull()
                error(detail?.takeIf { it.isNotBlank() }
                    ?: "Integrity verifier returned HTTP ${response.code}.")
            }
            parseVerifiedResponse(JSONObject(responseText))
        }
    }

    private fun parseVerifiedResponse(json: JSONObject): IntegrityUiState {
        if (!json.optBoolean("valid", false)) {
            error(json.optString("message", "The integrity response could not be validated."))
        }
        val decision = json.optString("decision", "REVIEW")
        val remediationType = json.optInt("remediationDialogType", 0).takeIf { it > 0 }
        lastRemediationType = remediationType
        return IntegrityUiState(
            status = if (decision == "TRUSTED") {
                IntegrityCheckStatus.TRUSTED
            } else {
                IntegrityCheckStatus.REVIEW
            },
            message = json.optString("message", "Server verification completed."),
            appIntegrity = json.optString("appIntegrity").takeIf { it.isNotBlank() },
            deviceIntegrity = json.optJSONArray("deviceIntegrity").toStringList(),
            playProtectVerdict = json.optString("playProtectVerdict").takeIf { it.isNotBlank() },
            appAccessRisk = json.optJSONArray("appAccessRisk").toStringList(),
            licensingVerdict = json.optString("licensingVerdict").takeIf { it.isNotBlank() },
            checkedAtMillis = System.currentTimeMillis(),
            remediationDialogType = remediationType
        )
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let(::add)
            }
        }
    }

    private fun Throwable.toUserMessage(): String {
        return message?.takeIf { it.isNotBlank() }?.take(240)
            ?: "Play Integrity could not complete. Check Play distribution, network access, and verifier configuration."
    }

    internal companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        fun canonicalAction(
            packageName: String,
            actionId: String,
            timestampMillis: Long
        ): String = "$packageName|$actionId|$timestampMillis|environment_check"
    }
}

private suspend fun <T> Task<T>.awaitResult(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        if (continuation.isActive) continuation.resume(result)
    }
    addOnFailureListener { error ->
        if (continuation.isActive) continuation.resumeWithException(error)
    }
}
