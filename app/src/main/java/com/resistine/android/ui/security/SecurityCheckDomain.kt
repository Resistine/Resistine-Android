package com.resistine.android.ui.security

/**
 * Represents the result of a single security check.
 */
enum class SecurityStatus {
    /** The check passed and the state is secure. */
    SAFE,
    /** A potential risk or sub-optimal configuration was detected. */
    WARNING,
    /** A significant security risk was detected. */
    DANGER,
    /** The status could not be determined or requires user manual check. */
    INFO;

    /**
     * Returns a penalty value associated with each status for scoring.
     */
    fun getPenalty(): Int = when (this) {
        SAFE -> 0
        INFO -> 0
        WARNING -> 5
        DANGER -> 25
    }
}

/**
 * Categories for grouping security checks.
 */
enum class SecurityCategory(val title: String) {
    UPDATES("Updates"),
    DEVICE_LOCK("Device Lock"),
    MALWARE_PROTECTION("App & Malware Protection"),
    RADIO_SURFACE("Radio Attack Surface"),
    LOCATION_PRIVACY("Location Privacy")
}

/**
 * Data model for a single security check item.
 *
 * @property id Unique identifier for the check.
 * @property category The group this check belongs to.
 * @property title Human-readable name of the check.
 * @property value Current detected value or state description.
 * @property status Severity level of the current state.
 * @property description Optional detailed explanation or remediation steps.
 */
data class SecurityCheckItem(
    val id: String,
    val category: SecurityCategory,
    val title: String,
    val value: String,
    val status: SecurityStatus,
    val description: String? = null
)
