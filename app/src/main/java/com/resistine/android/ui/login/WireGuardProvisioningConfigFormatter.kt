package com.resistine.android.ui.login

import org.json.JSONArray
import org.json.JSONObject

internal object WireGuardProvisioningConfigFormatter {
    fun format(jsonConfig: String, privateKey: String): String {
        val root = JSONObject(jsonConfig)
        val interfaceConfig = root.requireObject("Interface")
        val peer = root.requireObject("Peer")

        val addresses = interfaceConfig.stringList("Address", required = true)
        val dnsServers = interfaceConfig.stringList("DNS", required = false)
            .map { it.removePrefix("/") }
        val publicKey = peer.requiredString("PublicKey")
        val allowedIps = peer.stringList("AllowedIPs", required = true)
        val endpoint = peer.requiredString("Endpoint")
        val safePrivateKey = privateKey.safeConfigValue("PrivateKey")

        return buildString {
            appendLine("[Interface]")
            appendLine("PrivateKey = $safePrivateKey")
            appendLine("Address = ${addresses.joinToString(", ")}")
            if (dnsServers.isNotEmpty()) {
                appendLine("DNS = ${dnsServers.joinToString(", ")}")
            }
            appendLine()
            appendLine("[Peer]")
            appendLine("PublicKey = $publicKey")
            appendLine("AllowedIPs = ${allowedIps.joinToString(", ")}")
            append("Endpoint = $endpoint")
        }
    }

    private fun JSONObject.requireObject(key: String): JSONObject {
        return optJSONObject(key)
            ?: throw IllegalArgumentException("WireGuard configuration is missing $key")
    }

    private fun JSONObject.requiredString(key: String): String {
        val value = optString(key).takeIf { it.isNotBlank() }
            ?: throw IllegalArgumentException("WireGuard configuration is missing $key")
        return value.safeConfigValue(key)
    }

    private fun JSONObject.stringList(key: String, required: Boolean): List<String> {
        if (!has(key) || isNull(key)) {
            if (required) throw IllegalArgumentException("WireGuard configuration is missing $key")
            return emptyList()
        }

        val values = when (val raw = get(key)) {
            is JSONArray -> (0 until raw.length()).map { index -> raw.getString(index) }
            is String -> parseStringList(raw)
            else -> throw IllegalArgumentException("WireGuard $key must be a string or array")
        }.map { it.trim().safeConfigValue(key) }
            .filter(String::isNotBlank)

        if (required && values.isEmpty()) {
            throw IllegalArgumentException("WireGuard configuration has no $key values")
        }
        return values
    }

    private fun parseStringList(raw: String): List<String> {
        val trimmed = raw.trim()
        if (trimmed.startsWith('[') && trimmed.endsWith(']')) {
            return runCatching {
                val array = JSONArray(trimmed)
                (0 until array.length()).map { index -> array.getString(index) }
            }.getOrElse {
                throw IllegalArgumentException("WireGuard list value is invalid", it)
            }
        }
        return trimmed.split(',')
    }

    private fun String.safeConfigValue(field: String): String {
        require('\r' !in this && '\n' !in this) { "WireGuard $field contains an invalid line break" }
        return trim()
    }
}
