//[app](../../index.md)/[com.resistine.android.security](index.md)

# Package-level declarations

## Types

| Name | Summary |
|---|---|
| [CryptoManager](-crypto-manager/index.md) | [androidJvm]<br>object [CryptoManager](-crypto-manager/index.md)<br>Manager handling cryptographic encryption and decryption of sensitive configuration and user data using Android KeyStore and AES-GCM. |
| [LogLevel](-log-level/index.md) | [androidJvm]<br>enum [LogLevel](-log-level/index.md) : [Enum](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-enum/index.html)&lt;[LogLevel](-log-level/index.md)&gt; <br>Syslog severity levels (RFC 5424). |
| [SystemEventLogger](-system-event-logger/index.md) | [androidJvm]<br>class [SystemEventLogger](-system-event-logger/index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))<br>Logger observing system broadcasts (screen state, battery changes, connectivity changes, and package events) and reporting them to the [WazuhAgent](-wazuh-agent/index.md). |
| [WazuhAgent](-wazuh-agent/index.md) | [androidJvm]<br>class [WazuhAgent](-wazuh-agent/index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))<br>Security agent responsible for recording system logs, security posture checks, device info, and persisting log entries for Wazuh transmission. |
| [WazuhCrypto](-wazuh-crypto/index.md) | [androidJvm]<br>object [WazuhCrypto](-wazuh-crypto/index.md)<br>Cryptographic utility for building and encrypting Wazuh protocol packets (AES-256-CBC, Zlib compression, MD5 hashing). |