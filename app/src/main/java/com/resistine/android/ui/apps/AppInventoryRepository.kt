package com.resistine.android.ui.apps

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

internal class AppInventoryRepository(
    private val context: Context
) {

    init {
        // App inventories are sensitive and should not be included in Android Auto Backup.
        runCatching { File(context.filesDir, RESULTS_FILE).delete() }
    }

    fun loadInstalledApps(
        showSystemApps: Boolean,
        persistedScans: Map<String, PersistedAppScan> = loadPersistedScans()
    ): List<AppEntry> {
        val flags = PackageManager.GET_PERMISSIONS or PackageManager.GET_SERVICES
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getInstalledPackages(flags)
        }
        return packages.asSequence()
            .filter { info ->
                val appInfo = info.applicationInfo
                appInfo != null && (showSystemApps || !ScanUtils.isSystemApp(appInfo))
            }
            .map { info ->
                val label = info.applicationInfo?.loadLabel(context.packageManager)?.toString().orEmpty()
                val persisted = persistedScans[info.packageName]
                    ?.takeIf { it.lastUpdateTime == info.lastUpdateTime }
                AppEntry(info, label, persisted?.result)
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun persistScans(scans: List<AppEntry>) {
        val entries = JSONArray()
        scans.forEach { entry ->
            val result = entry.scanResult ?: return@forEach
            entries.put(JSONObject().apply {
                put("package", entry.packageInfo.packageName)
                put("lastUpdateTime", entry.packageInfo.lastUpdateTime)
                put("score", result.score)
                put("verdict", result.verdict.name)
                put("provenance", result.provenance.name)
                result.installerPackage?.let { put("installerPackage", it) }
                put("identityConfidence", result.identityConfidence.name)
                put("scannedAtMillis", result.scannedAtMillis)
                put("permissions", JSONArray(result.highRiskPermissions))
                put("badges", JSONArray().apply {
                    result.badges.forEach { badge ->
                        put(JSONObject().apply {
                            put("type", badge.type.name)
                            put("label", badge.label)
                            badge.description?.let { put("description", it) }
                        })
                    }
                })
            })
        }
        val root = JSONObject().apply {
            put("rulesVersion", RULES_VERSION)
            put("scans", entries)
        }
        runCatching { resultsFile().writeText(root.toString()) }
    }

    fun loadPersistedScans(): Map<String, PersistedAppScan> {
        val file = resultsFile()
        if (!file.exists()) return emptyMap()
        return runCatching {
            val root = JSONObject(file.readText())
            if (root.optInt("rulesVersion", -1) != RULES_VERSION) return@runCatching emptyMap()
            val scans = root.optJSONArray("scans") ?: return@runCatching emptyMap()
            buildMap {
                for (index in 0 until scans.length()) {
                    val json = scans.optJSONObject(index) ?: continue
                    val packageName = json.optString("package")
                    if (packageName.isBlank()) continue
                    val verdict = enumValueOrDefault(
                        json.optString("verdict"),
                        RiskVerdict.UNKNOWN
                    )
                    val provenance = enumValueOrDefault(
                        json.optString("provenance"),
                        InstallProvenance.UNKNOWN
                    )
                    val identity = enumValueOrDefault(
                        json.optString("identityConfidence"),
                        AppIdentityConfidence.UNVERIFIED
                    )
                    val permissions = json.optJSONArray("permissions").toStringList()
                    val badges = json.optJSONArray("badges").toBadges()
                    put(
                        packageName,
                        PersistedAppScan(
                            lastUpdateTime = json.optLong("lastUpdateTime", 0L),
                            result = AppScanResult(
                                score = json.optInt("score", 0),
                                verdict = verdict,
                                badges = badges,
                                highRiskPermissions = permissions,
                                provenance = provenance,
                                installerPackage = json.optString("installerPackage")
                                    .takeIf { it.isNotBlank() },
                                identityConfidence = identity,
                                scannedAtMillis = json.optLong("scannedAtMillis", 0L)
                            )
                        )
                    )
                }
            }
        }.getOrDefault(emptyMap())
    }

    private fun resultsFile(): File = File(context.noBackupFilesDir, RESULTS_FILE)

    private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, default: T): T {
        return runCatching { enumValueOf<T>(value) }.getOrDefault(default)
    }

    private fun JSONArray?.toStringList(): List<String> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                optString(index).takeIf { it.isNotBlank() }?.let { add(it) }
            }
        }
    }

    private fun JSONArray?.toBadges(): List<Badge> {
        if (this == null) return emptyList()
        return buildList {
            for (index in 0 until length()) {
                val json = optJSONObject(index) ?: continue
                val type = runCatching { BadgeType.valueOf(json.optString("type")) }.getOrNull() ?: continue
                add(
                    Badge(
                        type = type,
                        label = json.optString("label"),
                        description = json.optString("description").takeIf { it.isNotBlank() }
                    )
                )
            }
        }
    }

    companion object {
        const val RULES_VERSION = 3
        private const val RESULTS_FILE = "app_posture_results.json"
    }
}

internal data class PersistedAppScan(
    val lastUpdateTime: Long,
    val result: AppScanResult
)
