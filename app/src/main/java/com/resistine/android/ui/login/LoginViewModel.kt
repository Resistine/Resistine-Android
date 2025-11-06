package com.resistine.android.ui.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.resistine.android.R
import com.resistine.android.security.CryptoManager
import com.wireguard.crypto.KeyPair
import org.json.JSONObject

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    var email = MutableLiveData<String>()
    val otpSent = MutableLiveData<Boolean>()
    val loginSuccess = MutableLiveData<Boolean>()
    val loading = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String?>()
    val wireguardConfig = MutableLiveData<String?>()

    companion object {
        private const val VPN_NAME = "testVPNapi"
    }

    fun sendOtp(email: String) {
        loading.postValue(true)
        NetworkClient.sendOtp(email) { isSuccess, error ->
            loading.postValue(false)
            if (isSuccess) {
                otpSent.postValue(true)
            } else {
                errorMessage.postValue(error ?: getApplication<Application>().getString(R.string.failed_to_send_code))
            }
        }
    }

    fun verifyOtp(otp: String) {
        loading.postValue(true)
        val userEmail = email.value
        if (userEmail == null) {
            errorMessage.postValue(getApplication<Application>().getString(R.string.email_not_available))
            loading.postValue(false)
            return
        }

        val keyPair = KeyPair()
        val publicKey = keyPair.publicKey.toBase64()

        NetworkClient.verifyOtp(userEmail, otp, publicKey, VPN_NAME) { isSuccess, error, config ->
            loading.postValue(false)
            if (isSuccess && config != null) {
                try {
                    val formattedConfig = formatConfigForWireguard(config, keyPair.privateKey.toBase64())
                    CryptoManager.saveEncryptedConfig(getApplication(), formattedConfig)
                    wireguardConfig.postValue(formattedConfig)
                    loginSuccess.postValue(true)
                } catch (e: Exception) {
                    errorMessage.postValue(getApplication<Application>().getString(R.string.error_processing_config, e.message))
                }
            } else {
                errorMessage.postValue(error ?: getApplication<Application>().getString(R.string.verification_failed))
            }
        }
    }

    private fun formatConfigForWireguard(jsonConfig: String, privateKey: String): String {
        val configJson = JSONObject(jsonConfig)
        val peer = configJson.getJSONObject("Peer")
        val interfaceJson = configJson.getJSONObject("Interface")

        val peerPublicKey = peer.getString("PublicKey")
        val endpoint = peer.getString("Endpoint")
        val allowedIPsArray = peer.getJSONArray("AllowedIPs")
        val allowedIPsList = (0 until allowedIPsArray.length()).map { allowedIPsArray.getString(it) }
        val allowedIPs = allowedIPsList.joinToString(", ")


        val interfaceAddress = interfaceJson.getString("Address")
        val dnsArray = interfaceJson.getJSONArray("DNS")
        val dnsList = (0 until dnsArray.length()).map { dnsArray.getString(it) }
        val interfaceDns = dnsList.joinToString(", ")

        return """
            [Interface]
            PrivateKey = $privateKey
            Address = $interfaceAddress
            DNS = $interfaceDns

            [Peer]
            PublicKey = $peerPublicKey
            AllowedIPs = $allowedIPs
            Endpoint = $endpoint
        """.trimIndent()
    }
}
