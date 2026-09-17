package com.resistine.android.network

/**
 * Result of validating whether a remote Wazuh manager endpoint is ready for communication.
 *
 * @property ready True if the endpoint is fully valid and ready; false otherwise.
 * @property issues List of validation issue descriptions, if any.
 */
data class WazuhRemoteReadiness(
    val ready: Boolean,
    val issues: List<String>
)

/**
 * Validator for checking remote Wazuh manager endpoint readiness.
 */
object WazuhRemoteReadinessValidator {

    /**
     * Validates a given [WazuhManagerEndpoint].
     *
     * @param endpoint The [WazuhManagerEndpoint] to validate.
     * @return A [WazuhRemoteReadiness] result containing validation status and issues.
     */
    fun validate(endpoint: WazuhManagerEndpoint): WazuhRemoteReadiness {
        val issues = mutableListOf<String>()

        runCatching { endpoint.validate() }
            .exceptionOrNull()
            ?.message
            ?.let(issues::add)

        return WazuhRemoteReadiness(ready = issues.isEmpty(), issues = issues)
    }
}
