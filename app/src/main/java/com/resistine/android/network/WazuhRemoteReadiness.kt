package com.resistine.android.network

data class WazuhRemoteReadiness(
    val ready: Boolean,
    val issues: List<String>
)

object WazuhRemoteReadinessValidator {
    fun validate(endpoint: WazuhManagerEndpoint): WazuhRemoteReadiness {
        val issues = mutableListOf<String>()

        runCatching { endpoint.validate() }
            .exceptionOrNull()
            ?.message
            ?.let(issues::add)

        return WazuhRemoteReadiness(ready = issues.isEmpty(), issues = issues)
    }
}
