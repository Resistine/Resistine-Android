# Resistine Android App

An Android application by Resistine. This app provides a simple home screen, an apps view, and a VPN screen that can establish a WireGuard tunnel using a configuration shipped in the app assets. It also displays helpful device and network info such as public IP, device model, Android version, battery level, and rough geolocation (via an IP geolocation service).

## Overview

- Package: `com.resistine.android`
- Min SDK: 24
- Target/Compile SDK: 36
- Language: Kotlin (JVM target 17)
- Build system: Gradle 8.13, Android Gradle Plugin 8.13.0
- UI: Material Components, ViewBinding, AndroidX Navigation with DrawerLayout
- VPN: WireGuard Go backend (`com.wireguard.android:tunnel`)

## Features

- Home screen with quick information and navigation drawer
- VPN screen to request VPN permission and connect/disconnect a WireGuard tunnel
- Reads WireGuard config from `app/src/main/assets/myvpn.conf`
- Shows public IP, device model, Android version, and battery level
- Retrieves approximate location string via IP geolocation service

## Tech stack

- Kotlin 2.0.21
- AndroidX Core, AppCompat, RecyclerView, ConstraintLayout
- Material Components
- AndroidX Lifecycle (ViewModel, LiveData)
- AndroidX Navigation (Fragment + UI)
- Google Play services Location
- OkHttp 4.12.0
- WireGuard Tunnel 1.0.20230706

See versions in `gradle/libs.versions.toml` and dependencies in `app/build.gradle.kts`.

## Project layout

- `app/src/main/java/com/resistine/android/MainActivity.kt` — Hosts navigation drawer and NavHostFragment
- `app/src/main/java/com/resistine/android/ui/home/*` — Home fragment and adapters
- `app/src/main/java/com/resistine/android/ui/vpn/*` — VPN fragment and view model (WireGuard integration)
- `app/src/main/java/com/resistine/android/ui/apps/*` — Apps fragment and related classes
- `app/src/main/java/com/resistine/android/NetworkUtils.kt` — Fetches public IP via OkHttp
- `app/src/main/res/navigation/mobile_navigation.xml` — Navigation graph (Home, VPN, Apps)
- `app/src/main/AndroidManifest.xml` — Permissions and app metadata
- `app/src/main/assets/` — Place your `myvpn.conf` WireGuard config here

## Requirements

- JDK 17
- Android SDK Platform 36 (Android 15) and build-tools for SDK 36
- Gradle wrapper (provided) 8.13
- Android Studio (current stable) or newer with Kotlin support

## Setup

1) Clone and open the project in Android Studio, or use the Gradle wrapper on the command line.
2) Create a WireGuard configuration at `app/src/main/assets/myvpn.conf` (see below).
3) Ensure a device or emulator with Google Play services (for Location) if you want location info.

## Building and running

Android Studio:
- Open the project, let Gradle sync, then Run ▶ to install on a connected device.

Command line (Linux/macOS):

```bash
# Build debug APK
./gradlew :app:assembleDebug

# Install on a connected device
./gradlew :app:installDebug
```

Tests:

```bash
# Run unit tests
./gradlew test

# Run instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest
```

## Configuration: WireGuard

The VPN screen uses the WireGuard Go backend to bring up a tunnel using a config file named `myvpn.conf` located in the app assets. Create the file at:

```
app/src/main/assets/myvpn.conf
```

Minimal example (replace with your real keys and server):

```
[Interface]
PrivateKey = <base64-private-key>
Address = 10.0.0.2/32
DNS = 1.1.1.1

[Peer]
PublicKey = <server-public-key>
AllowedIPs = 0.0.0.0/0, ::/0
Endpoint = your.server.example.com:51820
PersistentKeepalive = 25
```

On first connect, Android will ask for VPN permission; accept to proceed. If you see an error like `UNABLE_TO_START_VPN`, try granting the permission again and reconnect.

## Permissions

Declared in `AndroidManifest.xml`:

- `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE` — Networking
- `FOREGROUND_SERVICE` — For potential foreground operations
- `QUERY_ALL_PACKAGES` — Used internally; suppressed with tools:ignore
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` — Used for coarse location text; location is derived via an IP geolocation service in code

At runtime, the app requests the VPN preparation permission via `VpnService.prepare(...)` on the VPN screen.

## Build configuration highlights

- compileSdk = 36, targetSdk = 36, minSdk = 24
- Java/Kotlin: JVM target 17, `isCoreLibraryDesugaringEnabled = true`
- ViewBinding enabled
- Release build type has minify disabled with default ProGuard rules

## Troubleshooting

- VPN won’t start: Ensure you accepted the VPN permission dialog and that `myvpn.conf` is valid.
- Location string shows an error: Network connectivity must be available; IP geolocation service may rate limit requests.
- Gradle sync issues: Use JDK 17 and the bundled/compatible Android Gradle Plugin (8.13.0) as defined in the project.

## Contributing

Issues and pull requests are welcome. Please follow typical Kotlin/Android style, keep changes focused, and include a brief description.

## Security

See `SECURITY.md`. To report a vulnerability, email security@resistine.com. Only the newest version is currently supported with security updates.

## License

    Resistine Android App
    Copyright (C) 2025 Resistine

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation; either version 2 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License along
    with this program; if not, write to the Free Software Foundation, Inc.,
    51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.

This project includes a `LICENSE` file in the repository root. See that file for details.


