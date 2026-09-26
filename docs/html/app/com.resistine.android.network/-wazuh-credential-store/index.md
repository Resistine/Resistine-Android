//[app](../../../index.md)/[com.resistine.android.network](../index.md)/[WazuhCredentialStore](index.md)

# WazuhCredentialStore

class [WazuhCredentialStore](index.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html))

Secure store for persisting encrypted Wazuh agent credentials using Android KeyStore and AES-GCM.

#### Parameters

androidJvm

| | |
|---|---|
| context | Application context. |

## Constructors

| | |
|---|---|
| [WazuhCredentialStore](-wazuh-credential-store.md) | [androidJvm]<br>constructor(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html)) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Functions

| Name | Summary |
|---|---|
| [clear](clear.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [clear](clear.md)()<br>Clears all stored Wazuh credentials. |
| [load](load.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [load](load.md)(): [WazuhAgentCredentials](../-wazuh-agent-credentials/index.md)?<br>Loads and decrypts stored Wazuh agent credentials. |
| [save](save.md) | [androidJvm]<br>@[Synchronized](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.jvm/-synchronized/index.html)<br>fun [save](save.md)(credentials: [WazuhAgentCredentials](../-wazuh-agent-credentials/index.md))<br>Encrypts and saves Wazuh agent credentials. |