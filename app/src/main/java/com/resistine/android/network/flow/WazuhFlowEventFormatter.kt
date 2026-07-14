package com.resistine.android.network.flow

object WazuhFlowEventFormatter {
    fun format(record: FlowRecord): String {
        val hasPorts = record.protocol == FlowProtocol.TCP || record.protocol == FlowProtocol.UDP
        val fields = linkedMapOf<String, Any?>(
            "event_type" to "resistine_flow",
            "event_version" to "1.0",
            "flow_id" to record.id,
            "timestamp_start" to record.timestampStartMillis,
            "timestamp_end" to record.timestampEndMillis,
            "duration_ms" to record.durationMillis,
            "ip_version" to record.ipVersion,
            "protocol" to record.protocol.name,
            "src_ip" to record.srcIp,
            "src_port" to record.srcPort.takeIf { hasPorts },
            "dst_ip" to record.dstIp,
            "dst_port" to record.dstPort.takeIf { hasPorts },
            "bytes_out" to record.bytesOut,
            "bytes_in" to record.bytesIn,
            "packets_out" to record.packetsOut,
            "packets_in" to record.packetsIn,
            "network_type" to record.networkType.name.lowercase(),
            "app_uid" to record.appUid,
            "app_package" to record.appPackage,
            "vpn_active" to record.vpnActive,
            "dns_query_name" to record.protocolEvidence.dnsQueryName,
            "dns_query_type" to record.protocolEvidence.dnsQueryType,
            "dns_response_code" to record.protocolEvidence.dnsResponseCode,
            "dns_answer_value" to record.protocolEvidence.dnsAnswerValue,
            "tls_sni" to record.protocolEvidence.tlsSni,
            "tls_alpn" to record.protocolEvidence.tlsAlpn,
            "http_host" to record.protocolEvidence.httpHost,
            "http_method" to record.protocolEvidence.httpMethod
        )
        return fields.entries.joinToString(prefix = "{", postfix = "}") { (key, value) ->
            "\"${escape(key)}\":${jsonValue(value)}"
        }
    }

    private fun jsonValue(value: Any?): String {
        return when (value) {
            null -> "null"
            is Number, is Boolean -> value.toString()
            else -> "\"${escape(value.toString())}\""
        }
    }

    private fun escape(value: String): String {
        val builder = StringBuilder(value.length + 16)
        value.forEach { char ->
            when (char) {
                '\\' -> builder.append("\\\\")
                '"' -> builder.append("\\\"")
                '\b' -> builder.append("\\b")
                '\u000C' -> builder.append("\\f")
                '\n' -> builder.append("\\n")
                '\r' -> builder.append("\\r")
                '\t' -> builder.append("\\t")
                else -> {
                    if (char.code < 0x20) {
                        builder.append("\\u").append(char.code.toString(16).padStart(4, '0'))
                    } else {
                        builder.append(char)
                    }
                }
            }
        }
        return builder.toString()
    }
}
