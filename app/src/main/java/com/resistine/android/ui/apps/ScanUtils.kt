package com.resistine.android.ui.apps

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.Signature
import android.os.Build
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.security.MessageDigest
import kotlin.math.abs
import kotlin.math.max

internal object ScanUtils {
    const val LOOKALIKE_THRESHOLD = 0.97
    private const val NAME_KEY_PREFIX = 3
    private const val NAME_LEN_BUCKET = 5

    val highRiskPermissions = setOf(
        "android.permission.READ_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.SEND_SMS",
        "android.permission.BIND_ACCESSIBILITY_SERVICE",
        "android.permission.REQUEST_INSTALL_PACKAGES",
        "android.permission.SYSTEM_ALERT_WINDOW",
        "android.permission.READ_CALL_LOG",
        "android.permission.WRITE_CALL_LOG",
        "android.permission.RECORD_AUDIO",
        "android.permission.READ_CONTACTS",
        "android.permission.WRITE_CONTACTS",
        "android.permission.READ_PHONE_STATE",
        "android.permission.CAMERA",
        "android.permission.ACCESS_FINE_LOCATION"
    )

    data class PermissionProfile(
        val id: String,
        val packageKeywords: List<String> = emptyList(),
        val labelKeywords: List<String> = emptyList(),
        val allowedPermissions: Set<String>
    ) {
        fun matches(packageName: String, label: String): Boolean {
            val pkg = packageName.lowercase()
            val name = label.lowercase()
            return packageKeywords.any { pkg.contains(it) } || labelKeywords.any { name.contains(it) }
        }
    }

    private val permissionProfiles = listOf(
        PermissionProfile(
            id = "camera",
            packageKeywords = listOf("camera", "photo", "photos", "gallery", "cam"),
            labelKeywords = listOf("camera", "photo", "photos", "gallery"),
            allowedPermissions = setOf(
                "android.permission.CAMERA",
                "android.permission.RECORD_AUDIO"
            )
        ),
        PermissionProfile(
            id = "messages",
            packageKeywords = listOf("sms", "mms", "messaging", "messages"),
            labelKeywords = listOf("sms", "mms", "message", "messages"),
            allowedPermissions = setOf(
                "android.permission.READ_SMS",
                "android.permission.RECEIVE_SMS",
                "android.permission.SEND_SMS"
            )
        ),
        PermissionProfile(
            id = "dialer",
            packageKeywords = listOf("dialer", "phone", "call"),
            labelKeywords = listOf("dialer", "phone", "call"),
            allowedPermissions = setOf(
                "android.permission.READ_CALL_LOG",
                "android.permission.WRITE_CALL_LOG",
                "android.permission.READ_PHONE_STATE"
            )
        ),
        PermissionProfile(
            id = "contacts",
            packageKeywords = listOf("contacts", "people"),
            labelKeywords = listOf("contacts", "people"),
            allowedPermissions = setOf(
                "android.permission.READ_CONTACTS",
                "android.permission.WRITE_CONTACTS"
            )
        ),
        PermissionProfile(
            id = "maps",
            packageKeywords = listOf("maps", "map", "navigation", "gps"),
            labelKeywords = listOf("map", "maps", "navigation", "gps"),
            allowedPermissions = setOf(
                "android.permission.ACCESS_FINE_LOCATION"
            )
        ),
        PermissionProfile(
            id = "recorder",
            packageKeywords = listOf("recorder", "voice", "audio"),
            labelKeywords = listOf("recorder", "voice", "audio"),
            allowedPermissions = setOf(
                "android.permission.RECORD_AUDIO"
            )
        )
    )

    fun allowedHighRiskPermissions(packageName: String, label: String): Set<String> {
        val allowed = HashSet<String>()
        for (profile in permissionProfiles) {
            if (profile.matches(packageName, label)) {
                allowed.addAll(profile.allowedPermissions)
            }
        }
        return allowed
    }

    private val permissionLabels = mapOf(
        "android.permission.READ_SMS" to "Read SMS",
        "android.permission.RECEIVE_SMS" to "Receive SMS",
        "android.permission.SEND_SMS" to "Send SMS",
        "android.permission.BIND_ACCESSIBILITY_SERVICE" to "Accessibility service",
        "android.permission.REQUEST_INSTALL_PACKAGES" to "Install apps",
        "android.permission.SYSTEM_ALERT_WINDOW" to "Display over other apps",
        "android.permission.READ_CALL_LOG" to "Read call log",
        "android.permission.WRITE_CALL_LOG" to "Write call log",
        "android.permission.RECORD_AUDIO" to "Record audio",
        "android.permission.READ_CONTACTS" to "Read contacts",
        "android.permission.WRITE_CONTACTS" to "Write contacts",
        "android.permission.READ_PHONE_STATE" to "Read phone state",
        "android.permission.CAMERA" to "Camera",
        "android.permission.ACCESS_FINE_LOCATION" to "Precise location"
    )

    fun normalizeName(name: String): String {
        val sb = StringBuilder(name.length)
        for (ch in name.lowercase()) {
            if (ch.isLetterOrDigit()) {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun nameKey(normalized: String): String {
        if (normalized.isEmpty()) return ""
        val prefix = if (normalized.length <= NAME_KEY_PREFIX) normalized else normalized.substring(0, NAME_KEY_PREFIX)
        val bucket = normalized.length / NAME_LEN_BUCKET
        return "$prefix|$bucket"
    }

    fun jaroWinkler(a: String, b: String): Double {
        if (a == b) return 1.0
        if (a.isEmpty() || b.isEmpty()) return 0.0
        val maxDist = max(0, max(a.length, b.length) / 2 - 1)
        val aMatches = BooleanArray(a.length)
        val bMatches = BooleanArray(b.length)
        var matches = 0
        for (i in a.indices) {
            val start = max(0, i - maxDist)
            val end = minOf(i + maxDist + 1, b.length)
            for (j in start until end) {
                if (bMatches[j]) continue
                if (a[i] != b[j]) continue
                aMatches[i] = true
                bMatches[j] = true
                matches++
                break
            }
        }
        if (matches == 0) return 0.0
        var t = 0
        var k = 0
        for (i in a.indices) {
            if (!aMatches[i]) continue
            while (!bMatches[k]) k++
            if (a[i] != b[k]) t++
            k++
        }
        val m = matches.toDouble()
        val jaro = (m / a.length + m / b.length + (m - t / 2.0) / m) / 3.0
        val prefix = commonPrefixLength(a, b, 4)
        return jaro + prefix * 0.1 * (1.0 - jaro)
    }

    private fun commonPrefixLength(a: String, b: String, maxPrefix: Int): Int {
        val limit = minOf(minOf(a.length, b.length), maxPrefix)
        var count = 0
        for (i in 0 until limit) {
            if (a[i] != b[i]) break
            count++
        }
        return count
    }

    fun isSimilarName(a: String, b: String, threshold: Double = LOOKALIKE_THRESHOLD): Boolean {
        if (a.isEmpty() || b.isEmpty()) return false
        if (abs(a.length - b.length) > 6) return false
        return jaroWinkler(a, b) >= threshold
    }

    fun apkSha256(paths: List<String>): List<String> {
        val hashes = ArrayList<String>(paths.size)
        for (path in paths) {
            val digest = MessageDigest.getInstance("SHA-256")
            BufferedInputStream(java.io.FileInputStream(path)).use { input ->
                val buffer = ByteArray(16 * 1024)
                while (true) {
                    val read = input.read(buffer)
                    if (read <= 0) break
                    digest.update(buffer, 0, read)
                }
            }
            hashes.add(digest.digest().toHexString())
        }
        return hashes
    }

    fun signatureSha256(packageInfo: PackageInfo): Set<String> {
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = packageInfo.signingInfo
            if (signingInfo == null) {
                emptyArray<Signature>()
            } else if (signingInfo.hasMultipleSigners()) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo.signingCertificateHistory
            }
        } else {
            packageInfo.signatures ?: emptyArray()
        }
        val result = HashSet<String>(signatures.size)
        for (signature in signatures) {
            val digest = MessageDigest.getInstance("SHA-256")
            result.add(digest.digest(signature.toByteArray()).toHexString())
        }
        return result
    }

    fun installerPackageName(context: Context, packageName: String): String? {
        val pm = context.packageManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            runCatching { pm.getInstallSourceInfo(packageName).installingPackageName }.getOrNull()
        } else {
            pm.getInstallerPackageName(packageName)
        }
    }

    fun loadRegistry(context: Context, assetName: String): RegistryIndex {
        val byPackage = HashMap<String, RegistryEntry>(8192)
        val nameIndex = HashMap<String, MutableList<RegistryNameEntry>>()
        context.assets.open(assetName).use { input ->
            BufferedReader(InputStreamReader(input)).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty()) {
                        val entry = RegistryJsonParser.parseLine(trimmed)
                        if (entry != null) {
                            byPackage[entry.packageName] = entry
                            val normalized = normalizeName(entry.name)
                            if (normalized.isNotEmpty()) {
                                val key = nameKey(normalized)
                                val list = nameIndex.getOrPut(key) { ArrayList() }
                                list.add(RegistryNameEntry(entry.packageName, entry.name, normalized))
                            }
                        }
                    }
                    line = reader.readLine()
                }
            }
        }
        return RegistryIndex(byPackage, nameIndex)
    }

    private fun ByteArray.toHexString(): String {
        val result = StringBuilder(size * 2)
        for (b in this) {
            val value = b.toInt() and 0xFF
            if (value < 16) result.append('0')
            result.append(Integer.toHexString(value))
        }
        return result.toString()
    }

    fun permissionDisplayName(permission: String): String {
        return permissionLabels[permission] ?: permission.substringAfterLast('.')
            .replace('_', ' ')
            .lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}

internal fun PackageInfo.longVersionCodeCompat(): Long {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        longVersionCode
    } else {
        versionCode.toLong()
    }
}

internal object RegistryJsonParser {
    fun parseLine(line: String): RegistryEntry? {
        return try {
            val json = org.json.JSONObject(line)
            val packageName = json.optString("package")
            if (packageName.isBlank()) return null
            val name = json.optString("name")
            val versionCode = json.optLong("versionCode", 0L)
            val versionName = json.optString("versionName", "")
            val certArray = json.optJSONArray("certSha256")
            val certs = HashSet<String>(certArray?.length() ?: 0)
            if (certArray != null) {
                for (i in 0 until certArray.length()) {
                    val value = certArray.optString(i)
                    if (!value.isNullOrBlank()) {
                        certs.add(value.lowercase())
                    }
                }
            }
            RegistryEntry(
                packageName = packageName,
                name = name,
                versionCode = versionCode,
                versionName = versionName,
                certSha256 = certs,
                devName = json.optString("devName", ""),
                storeUrl = json.optString("storeUrl", ""),
                category = json.optString("category", "")
            )
        } catch (_: Exception) {
            null
        }
    }
}
