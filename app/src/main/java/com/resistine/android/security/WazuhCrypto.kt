package com.resistine.android.security

import java.security.MessageDigest
import java.util.zip.Deflater
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

object WazuhCrypto {

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

        // 1. Key derivation (exactly as in Swift: MD5 of the shared key)
        val cleanSharedKey = rawSharedKey.trim()
        val aesKeyHex = md5Hex(cleanSharedKey)

        // AES-256 uses 32 bytes. The hash has exactly 32 characters, so we take its ASCII representation.
        val secretKey = SecretKeySpec(aesKeyHex.toByteArray(Charsets.UTF_8), "AES")

        // 2. Header assembly with counter
        val rand1 = Random.nextInt(0, 65536)
        val counterHeader = String.format("%05d%010d:%04d:", rand1, globalCount, 0)

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

        val compressedBuffer = ByteArray(finMsgData.size + 1024)
        val compressedSize = deflater.deflate(compressedBuffer)
        deflater.end()

        val compressedData = ByteArray(compressedSize)
        System.arraycopy(compressedBuffer, 0, compressedData, 0, compressedSize)

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