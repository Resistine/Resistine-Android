package com.resistine.android.network

import java.io.ByteArrayInputStream
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

data class WazuhRemoteReadiness(
    val ready: Boolean,
    val issues: List<String>
)

object WazuhRemoteReadinessValidator {
    fun validate(endpoint: WazuhManagerEndpoint, managerCaPem: String?): WazuhRemoteReadiness {
        val issues = mutableListOf<String>()

        runCatching { endpoint.validate() }
            .exceptionOrNull()
            ?.message
            ?.let(issues::add)

        if (!managerCaPem.isNullOrBlank()) {
            runCatching {
                ByteArrayInputStream(managerCaPem.toByteArray(Charsets.US_ASCII)).use {
                    val certificate = CertificateFactory.getInstance("X.509")
                        .generateCertificate(it) as X509Certificate
                    certificate.checkValidity()
                }
            }.onFailure {
                issues += "Manager CA certificate is invalid or expired"
            }
        }

        return WazuhRemoteReadiness(ready = issues.isEmpty(), issues = issues)
    }
}
