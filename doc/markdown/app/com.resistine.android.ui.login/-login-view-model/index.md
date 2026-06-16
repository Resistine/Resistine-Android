//[app](../../../index.md)/[com.resistine.android.ui.login](../index.md)/[LoginViewModel](index.md)

# LoginViewModel

[androidJvm]\
class [LoginViewModel](index.md)(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) : [AndroidViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/AndroidViewModel.html)

## Constructors

| | |
|---|---|
| [LoginViewModel](-login-view-model.md) | [androidJvm]<br>constructor(application: [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)) |

## Types

| Name | Summary |
|---|---|
| [Companion](-companion/index.md) | [androidJvm]<br>object [Companion](-companion/index.md) |

## Properties

| Name | Summary |
|---|---|
| [email](email.md) | [androidJvm]<br>var [email](email.md): [MutableLiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/MutableLiveData.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)&gt; |
| [errorMessage](error-message.md) | [androidJvm]<br>val [errorMessage](error-message.md): [MutableLiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/MutableLiveData.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?&gt; |
| [isRegistrationSkipped](is-registration-skipped.md) | [androidJvm]<br>val [isRegistrationSkipped](is-registration-skipped.md): [MutableLiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/MutableLiveData.html)&lt;[Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)&gt; |
| [loading](loading.md) | [androidJvm]<br>val [loading](loading.md): [MutableLiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/MutableLiveData.html)&lt;[Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)&gt; |
| [loginSuccess](login-success.md) | [androidJvm]<br>val [loginSuccess](login-success.md): [MutableLiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/MutableLiveData.html)&lt;[Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)&gt; |
| [otpSent](otp-sent.md) | [androidJvm]<br>val [otpSent](otp-sent.md): [MutableLiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/MutableLiveData.html)&lt;[Boolean](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-boolean/index.html)&gt; |
| [wireguardConfig](wireguard-config.md) | [androidJvm]<br>val [wireguardConfig](wireguard-config.md): [MutableLiveData](https://developer.android.com/reference/kotlin/androidx/lifecycle/MutableLiveData.html)&lt;[String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)?&gt; |

## Functions

| Name | Summary |
|---|---|
| [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474) | [androidJvm]<br>open fun [addCloseable](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#264516373%2FFunctions%2F1606452474)(@[NonNull](https://developer.android.com/reference/kotlin/androidx/annotation/NonNull.html)closeable: [Closeable](https://developer.android.com/reference/kotlin/java/io/Closeable.html)) |
| [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) | [androidJvm]<br>open fun &lt;[T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) : [Application](https://developer.android.com/reference/kotlin/android/app/Application.html)&gt; [getApplication](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474)(): [T](../../com.resistine.android.ui.vpn/-vpn-view-model/index.md#1696759283%2FFunctions%2F1606452474) |
| [sendOtp](send-otp.md) | [androidJvm]<br>fun [sendOtp](send-otp.md)(email: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) |
| [skipRegistration](skip-registration.md) | [androidJvm]<br>fun [skipRegistration](skip-registration.md)() |
| [verifyOtp](verify-otp.md) | [androidJvm]<br>fun [verifyOtp](verify-otp.md)(otp: [String](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin/-string/index.html)) |