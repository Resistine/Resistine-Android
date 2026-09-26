//[app](../../../index.md)/[com.resistine.android.ui.wifi](../index.md)/[WifiTrustedBaselineManager](index.md)

# WifiTrustedBaselineManager

[androidJvm]\
object [WifiTrustedBaselineManager](index.md)

## Functions

| Name | Summary |
|---|---|
| [assess](assess.md) | [androidJvm]<br>fun [assess](assess.md)(profile: [TrustedWifiProfile](../../com.resistine.android.ui.vpn/-trusted-wifi-profile/index.md), observation: [WifiTrustedFingerprintObservation](../-wifi-trusted-fingerprint-observation/index.md)?, matchConfidence: [WifiMatchConfidence](../-wifi-match-confidence/index.md)): [WifiTrustAssessment](../-wifi-trust-assessment/index.md) |
| [isProfileDowngrade](is-profile-downgrade.md) | [androidJvm]<br>fun [isProfileDowngrade](is-profile-downgrade.md)(baseline: [WifiSecurityProfile](../-wifi-security-profile/index.md), current: [WifiSecurityProfile](../-wifi-security-profile/index.md)?): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [matchesPendingFingerprint](matches-pending-fingerprint.md) | [androidJvm]<br>fun [matchesPendingFingerprint](matches-pending-fingerprint.md)(profile: [TrustedWifiProfile](../../com.resistine.android.ui.vpn/-trusted-wifi-profile/index.md), observation: [WifiTrustedFingerprintObservation](../-wifi-trusted-fingerprint-observation/index.md)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [matchesStableFingerprint](matches-stable-fingerprint.md) | [androidJvm]<br>fun [matchesStableFingerprint](matches-stable-fingerprint.md)(profile: [TrustedWifiProfile](../../com.resistine.android.ui.vpn/-trusted-wifi-profile/index.md), observation: [WifiTrustedFingerprintObservation](../-wifi-trusted-fingerprint-observation/index.md)): [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html) |
| [updateProfile](update-profile.md) | [androidJvm]<br>fun [updateProfile](update-profile.md)(profile: [TrustedWifiProfile](../../com.resistine.android.ui.vpn/-trusted-wifi-profile/index.md), observation: [WifiTrustedFingerprintObservation](../-wifi-trusted-fingerprint-observation/index.md), allowLearning: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), approveNow: [Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html), nowMillis: [Long](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-long/index.html)): [TrustedWifiProfile](../../com.resistine.android.ui.vpn/-trusted-wifi-profile/index.md) |