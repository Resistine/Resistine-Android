package com.resistine.android.security

import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Locale
import java.util.zip.Deflater
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object WazuhCrypto {
    private val secureRandom = SecureRandom()

    private fun md5(input: ByteArray): ByteArray {
        return MessageDigest.getInstance("MD5").digest(input)
    }

    private fun md5Hex(input: String): String {
        val bytes = md5(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * @param agentId e.g. "004"
     * @param rawSharedKey Only the key itself (the 4th part), e.g. "e10adc3949ba..."
     * @param globalCount Message counter (for simplicity you can send an incrementally increasing number)
     */
    fun buildPacket(agentId: String, rawSharedKey: String, message: String, globalCount: Long = 1): ByteArray {
        require(agentId.matches(Regex("[0-9]{1,8}"))) { "Invalid Wazuh agent ID" }
        require(rawSharedKey.isNotBlank()) { "Wazuh agent key is required" }
        require(globalCount in 0..9_999_999_999L) { "Wazuh message counter is out of range" }

        // 1. Key derivation (exactly as in Swift: MD5 of the shared key)
        val cleanSharedKey = rawSharedKey.trim()
        val aesKeyHex = md5Hex(cleanSharedKey)

        // AES-256 uses 32 bytes. The hash has exactly 32 characters, so we take its ASCII representation.
        val secretKey = SecretKeySpec(aesKeyHex.toByteArray(Charsets.UTF_8), "AES")

        // 2. Header assembly with counter
        val rand1 = secureRandom.nextInt(65_536)
        val counterHeader = String.format(Locale.US, "%05d%010d:%04d:", rand1, globalCount, 0)

        // 3. Connection and protective MD5
        val combined = counterHeader + message
        val md5HexDigest = md5Hex(combined)

        // 4. Final message before compression
        val finMsgStr = md5HexDigest + combined
        val finMsgData = finMsgStr.toByteArray(Charsets.UTF_8)

        // 5. ZLIB Compression
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(finMsgData)
        deflater.finish()

        val compressedData = ByteArrayOutputStream(finMsgData.size).use { compressed ->
            val buffer = ByteArray(4096)
            while (!deflater.finished()) {
                val count = deflater.deflate(buffer)
                if (count <= 0 && deflater.needsInput()) break
                compressed.write(buffer, 0, count)
            }
            deflater.end()
            compressed.toByteArray()
        }

        // 6. Padding with exclamation marks (!) BEFORE compressed data, exactly according to Swift
        val bfsize = (8 - (compressedData.size % 8)) % 8
        val padCount = bfsize + 1 // At least 1 exclamation mark, so ReadSecMSG can find it

        val paddedCompressed = ByteArray(padCount + compressedData.size)
        for (i in 0 until padCount) {
            paddedCompressed[i] = 0x21.toByte() // ASCII '!'
        }
        System.arraycopy(compressedData, 0, paddedCompressed, padCount, compressedData.size)

        // 7. AES-256-CBC encryption with PKCS5Padding and hardcoded IV
        val ivBytes = "FEDCBA0987654321".toByteArray(Charsets.UTF_8)
        val ivSpec = IvParameterSpec(ivBytes)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") // Swift uses PKCS7, in Java it's PKCS5 (they are compatible)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

        val ciphertext = cipher.doFinal(paddedCompressed)

        // 8. Add Wazuh header "!ID!#AES:"
        val prefix = "!${agentId.trim()}!#AES:".toByteArray(Charsets.UTF_8)

        // Result is ready to be sent over TCP with 4-byte size header
        val finalPacket = ByteArray(prefix.size + ciphertext.size)
        System.arraycopy(prefix, 0, finalPacket, 0, prefix.size)
        System.arraycopy(ciphertext, 0, finalPacket, prefix.size, ciphertext.size)

        return finalPacket
    }
}
