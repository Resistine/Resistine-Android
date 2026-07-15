# Wi-Fi and app scanner validation

The scanners depend on Android framework behavior, OEM Wi-Fi implementations, Google Play distribution, user-granted access, and Play Console configuration. Unit tests cover deterministic policy and scoring; release readiness also requires this physical-device matrix.

## Required matrix

| Coverage | Minimum devices | What must pass |
| --- | --- | --- |
| Android 7-8 / API 24-27 | One physical device if still supported | App inventory, legacy permission path, cached Wi-Fi result behavior, no Private DNS claim |
| Android 9-10 / API 28-29 | One physical device | Private DNS state, AppOps overlay/usage access, location-gated Wi-Fi identity |
| Android 12 / API 31 | Pixel or AOSP-like and one OEM | Scan throttling/cooldown, WPA3/OWE parsing, package visibility |
| Android 13-14 / API 33-34 | Pixel and Samsung | Receiver registration, notification permission, package add/remove/update reconciliation |
| Android 15 / API 35 | Pixel and Samsung | Current target behavior, VPN route/DNS diagnostics, background/resume refresh |
| Google Play environment | Play-distributed internal-test build | PLAY_RECOGNIZED, LICENSED, device verdict, Play Protect and app-access-risk optional verdicts, every remediation path |
| Non-Play/AOSP environment | One emulator or device without Play Store | Integrity reports an actionable unavailable/error state; local scanners remain usable |

## Wi-Fi scenarios

- Connect to open, WPA2 Personal, WPA3 Personal, transition-mode, OWE, and enterprise networks where available. Confirm unknown information is shown as unknown and is never silently scored as secure.
- Repeatedly press refresh inside 30 seconds. Cached/passive data must stay visible and the cooldown must be reported without repeated active scan requests.
- Leave the screen open while Android or another app produces scan results. Nearby networks should update through the passive receiver without a new active request.
- Revoke and restore Location permission and disable/enable Location services. Identity, encryption coverage, and trust actions must degrade and recover accurately.
- Change BSSID, security mode, gateway, and frequency on a trusted SSID. Verify pending fingerprint behavior and explicit approval.
- Enable Private DNS automatic/hostname/off, then connect and disconnect VPN. Verify the displayed active route and DNS values against `adb shell dumpsys connectivity` and `adb shell dumpsys netd`.
- Configure a controlled `NETWORK_DIAGNOSTIC_URL`, test a successful HTTPS response, captive portal, TLS interception, DNS failure, and timeout. Confirm the probe is informational and never claims to be a full DNS-leak test.

## App scenarios

- Install, update, and uninstall an app while the Apps screen exists. The inventory should reconcile once and only the added/changed package should require a metadata scan.
- Toggle accessibility, notification listener, device admin, overlay, usage access, and battery optimization exemption. Returning to the screen must refresh effective access without enumerating packages again.
- Confirm a declared overlay capability without an active grant is distinct from an enabled overlay AppOp.
- Verify Play Store, recognized alternative store, local file, ADB, unknown installer, system, updated-system, debug, and legacy-target classifications against known test APKs.
- Measure the locally displayed full-scan and access-refresh duration/count on cold and warm runs. Investigate regressions over 25 percent on the same device and inventory.

## Play release and package visibility

`QUERY_ALL_PACKAGES` is required for the security inventory to assess installed apps comprehensively. Before each Play release, verify that the app's core purpose and store listing qualify under the current package-visibility policy, complete the Play declaration, retain the prominent on-device inventory disclosure, and confirm no inventory or scan result is uploaded. If the declaration is not accepted, the scanner must be explicitly reduced to visible/launchable packages and its coverage text changed; do not silently imply full coverage.

Keep screenshots and device/build identifiers with each matrix run. A release is not scanner-ready until the Play internal-test build, server verifier, and at least the current Pixel/Samsung rows pass.
