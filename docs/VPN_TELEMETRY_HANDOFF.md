# VPN Telemetry Handoff Checklist

This checklist covers the WireGuard forwarding, local flow capture, and optional
Wazuh delivery path. Run the final acceptance test on a physical Android device.

## Build

- Use Android Studio's bundled JDK and the Android SDK/NDK installed for the project.
- A normal app build verifies that the four checked-in native libraries exist, but
  it does not rebuild them from Go source.
- After any change under `app/src/main/wireguard-go`, rebuild all Android ABIs:

```powershell
.\gradlew.bat buildWireGuardTelemetryNative
```

- Then run the local verification and create the debug APK:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
```

- The native outputs are:
  `app/src/main/jniLibs/{armeabi-v7a,arm64-v8a,x86,x86_64}/libwg-go-telemetry.so`.
  Commit Go source and all four rebuilt libraries together.

## External Contracts

- OTP registration must return a valid WireGuard interface address, DNS server,
  peer public key, endpoint, and AllowedIPs.
- The app installs exactly the routes in AllowedIPs. Split routes protect only
  those networks; full-tunnel service requires `0.0.0.0/0` and, when supported,
  `::/0` from the backend.
- Connection success requires a fresh WireGuard peer handshake.
- Flow events use `event_type: "resistine_flow"` and `event_version: "1.0"`.
- Remote Wazuh delivery requires the manager enrollment and log ports to be
  reachable through the configured WireGuard routes.
- "Wazuh socket writes" means the encrypted client socket accepted the record.
  Wazuh manager decoding, rule matching, and dashboard indexing must be verified
  separately on the manager.

## Physical-Device Acceptance

Before starting, install the newly built APK, select local queue delivery, and
open Settings > Telemetry diagnostics. Connecting starts a fresh diagnostic
session.

- Connect and confirm that the app reports a fresh WireGuard handshake.
- Browse several HTTPS sites and use at least one app other than Resistine.
- Generate DNS, TCP, and UDP traffic. Generate IPv6 traffic only when the VPN
  server advertises and routes IPv6.
- Confirm that browsing and other apps continue to work through every test.
- Switch from Wi-Fi to mobile data and back. Confirm the tunnel recovers and
  traffic continues.
- Disconnect, wait for the status to settle, and inspect Telemetry diagnostics.
  Native, segment, persistence, and forwarding queue depths must be zero.
- Reconnect, generate more traffic, and disconnect again. Generated counters
  must restart for the new session and no duplicate runtime or uploader should
  remain active.
- For offline delivery, make the Wazuh manager unavailable or use local queue
  mode, generate traffic, and confirm Pending Wazuh records increases. Restore
  remote delivery and confirm the queue drains without a Wazuh queue drop.
- Run a sustained transfer large enough to exercise several minutes of traffic.
  Connectivity must not stall and throughput should remain within 10 percent of
  the accepted release baseline under the same network conditions.

The completed session passes only when these counters are zero:

```text
nativeTelemetryDropped
segmentQueueDropped
segmentWriteFailures
wazuhQueueDropped
telemetryReaderFailures
packetsRejectedAfterClose
forwardQueueDropped
forwardQueueDiscardedOnStop
```

After disconnecting, run the persisted-flow acceptance check:

```powershell
.\gradlew.bat connectedDebugAndroidTest `
  "-Pandroid.testInstrumentationRunnerArguments.class=com.resistine.android.network.flow.FlowCaptureAcceptanceInstrumentedTest" `
  "-Pandroid.testInstrumentationRunnerArguments.runFlowAcceptance=true" `
  "-Pandroid.testInstrumentationRunnerArguments.requiredProtocols=TCP,UDP"
```

Add these arguments when the environment is expected to support them:

```powershell
"-Pandroid.testInstrumentationRunnerArguments.requireIpv6=true"
"-Pandroid.testInstrumentationRunnerArguments.requireAppAttribution=true"
```

Save the test output, device model, Android version, app commit, VPN endpoint,
network type, transfer size, transfer duration, and final diagnostic snapshot.

## Metadata Boundaries

- `network_type` reports the underlying Wi-Fi, cellular, or Ethernet transport.
- Per-app attribution uses Android's VPN owner lookup on Android 10 and newer.
- `app_uid` and `app_package` remain `null` for ICMP, unsupported Android
  versions, connections Android can no longer resolve, or lookups rejected by
  the platform.
- A UID can be populated while `app_package` remains `null` when multiple
  packages share that UID; selecting one package would be inaccurate.
- The native queues hold 8,192 packet copies per direction. They absorb expected
  traffic bursts without blocking WireGuard forwarding. Unbounded overload can
  still exhaust any finite queue, so the heavy-transfer zero-drop check remains
  a release requirement.

## Recovery

- Handshake timeout: verify endpoint reachability, peer keys, device clock, and
  backend AllowedIPs before changing the client.
- Native library version or missing JNI method: rebuild all four native ABIs,
  reinstall the APK, and verify the pinned backend test.
- Nonzero native drops: retain the diagnostic snapshot and traffic details;
  do not approve the build as complete.
- Pending Wazuh queue not draining: keep the VPN connected, verify manager route
  and ports, inspect Last Wazuh error, then retry without deleting local records.
- Metadata remains unknown: confirm the device is Android 10 or newer, Resistine
  is the active VPN, and traffic was generated after the tunnel handshake.
