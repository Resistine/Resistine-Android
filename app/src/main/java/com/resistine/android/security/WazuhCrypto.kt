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
     * @param agentId Např. "004"
     * @param rawSharedKey Pouze samotný klíč (ta 4. část), např. "e10adc3949ba..."
     * @param globalCount Počítadlo zpráv (pro jednoduchost můžeš posílat postupně se zvyšující číslo)
     */
    fun buildPacket(agentId: String, rawSharedKey: String, message: String, globalCount: Long = 1): ByteArray {

        // 1. Derivace klíče (přesně jako ve Swiftu: MD5 ze sdíleného klíče)
        val cleanSharedKey = rawSharedKey.trim()
        val aesKeyHex = md5Hex(cleanSharedKey)

        // AES-256 používá 32 bajtů. Hash má přesně 32 znaků, takže vezmeme jeho ASCII reprezentaci.
        val secretKey = SecretKeySpec(aesKeyHex.toByteArray(Charsets.UTF_8), "AES")

        // 2. Sestavení hlavičky s počítadlem
        val rand1 = Random.nextInt(0, 65536)
        val counterHeader = String.format("%05d%010d:%04d:", rand1, globalCount, 0)

        // 3. Spojení a ochranný MD5
        val combined = counterHeader + message
        val md5HexDigest = md5Hex(combined)

        // 4. Finální zpráva před kompresí
        val finMsgStr = md5HexDigest + combined
        val finMsgData = finMsgStr.toByteArray(Charsets.UTF_8)

        // 5. ZLIB Komprese
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(finMsgData)
        deflater.finish()

        val compressedBuffer = ByteArray(finMsgData.size + 1024)
        val compressedSize = deflater.deflate(compressedBuffer)
        deflater.end()

        val compressedData = ByteArray(compressedSize)
        System.arraycopy(compressedBuffer, 0, compressedData, 0, compressedSize)

        // 6. Padding vykřičníky (!) PŘED komprimovaná data, přesně podle Swiftu
        val bfsize = (8 - (compressedData.size % 8)) % 8
        val padCount = bfsize + 1 // Alespoň 1 vykřičník, aby ho ReadSecMSG našel

        val paddedCompressed = ByteArray(padCount + compressedData.size)
        for (i in 0 until padCount) {
            paddedCompressed[i] = 0x21.toByte() // ASCII '!'
        }
        System.arraycopy(compressedData, 0, paddedCompressed, padCount, compressedData.size)

        // 7. Šifrování AES-256-CBC s PKCS5Padding a hardcoded IV
        val ivBytes = "FEDCBA0987654321".toByteArray(Charsets.UTF_8)
        val ivSpec = IvParameterSpec(ivBytes)

        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding") // Swift používá PKCS7, v Javě je to PKCS5 (jsou kompatibilní)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec)

        val ciphertext = cipher.doFinal(paddedCompressed)

        // 8. Přidání Wazuh hlavičky "!ID!#AES:"
        val prefix = "!${agentId.trim()}!#AES:".toByteArray(Charsets.UTF_8)

        // Výsledek je připraven k odeslání přes TCP s 4-bajtovou hlavičkou velikosti
        val finalPacket = ByteArray(prefix.size + ciphertext.size)
        System.arraycopy(prefix, 0, finalPacket, 0, prefix.size)
        System.arraycopy(ciphertext, 0, finalPacket, prefix.size, ciphertext.size)

        return finalPacket
    }
}