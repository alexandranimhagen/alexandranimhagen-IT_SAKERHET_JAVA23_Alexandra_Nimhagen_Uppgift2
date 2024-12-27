package com.jwtuppg.security

import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import javax.crypto.spec.IvParameterSpec
import android.util.Base64
import java.security.SecureRandom

object EncryptionUtil {
    private const val ALGORITHM = "AES"
    private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
    private const val KEY_SIZE = 256

    fun generateNewKey(): String {
        val keyGen = KeyGenerator.getInstance(ALGORITHM)
        keyGen.init(KEY_SIZE)
        val secretKey = keyGen.generateKey()
        return keyToString(secretKey)
    }

    fun keyToString(secretKey: SecretKey): String {
        return Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
    }

    fun stringToKey(encodedKey: String): SecretKey {
        val decodedKey = Base64.decode(encodedKey, Base64.DEFAULT)
        return SecretKeySpec(decodedKey, ALGORITHM)
    }

    fun encrypt(message: String, secretKey: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secret = stringToKey(secretKey)

        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        val ivSpec = IvParameterSpec(iv)

        cipher.init(Cipher.ENCRYPT_MODE, secret, ivSpec)
        val encryptedBytes = cipher.doFinal(message.toByteArray())

        val combined = iv + encryptedBytes
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedMessage: String, secretKey: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val secret = stringToKey(secretKey)

        val decodedMessage = Base64.decode(encryptedMessage, Base64.DEFAULT)

        val iv = decodedMessage.copyOfRange(0, 16)
        val ivSpec = IvParameterSpec(iv)

        val encryptedBytes = decodedMessage.copyOfRange(16, decodedMessage.size)

        cipher.init(Cipher.DECRYPT_MODE, secret, ivSpec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)

        return String(decryptedBytes)
    }
}
