package com.example.allinone.backup

import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import android.util.Base64
import android.util.Log

object EncryptionUtils {
    private const val TAG = "EncryptionUtils"
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val IV_LENGTH_GCM = 12
    private const val IV_LENGTH_CBC = 16
    private const val TAG_LENGTH = 128

    fun encrypt(data: String, password: CharArray): String {
        val salt = ByteArray(SALT_LENGTH).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(IV_LENGTH_GCM).apply { SecureRandom().nextBytes(this) }
        
        val key = deriveKey(password, salt, ITERATION_COUNT)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))
        
        val encryptedData = cipher.doFinal(data.toByteArray())
        
        val combined = salt + iv + encryptedData
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decrypt(encryptedDataStr: String, password: CharArray): String {
        val cleanedData = encryptedDataStr.trim().replace("\n", "").replace("\r", "")
        
        // Strategy: Try common iteration counts and encryption modes
        val iterationsToTry = listOf(ITERATION_COUNT, 1000, 65536)
        
        for (iterations in iterationsToTry) {
            // Try GCM first (Modern)
            try {
                return performDecryptionGCM(cleanedData, password, iterations)
            } catch (e: Exception) {
                Log.d(TAG, "GCM decryption failed for iterations $iterations: ${e.message}")
            }
            
            // Try CBC fallback (Legacy)
            try {
                return performDecryptionCBC(cleanedData, password, iterations)
            } catch (e: Exception) {
                Log.d(TAG, "CBC decryption failed for iterations $iterations: ${e.message}")
            }
        }
        
        throw Exception("Could not decrypt backup. Please check your password.")
    }

    private fun performDecryptionGCM(data: String, password: CharArray, iterations: Int): String {
        val decoded = Base64.decode(data, Base64.DEFAULT)
        if (decoded.size < SALT_LENGTH + IV_LENGTH_GCM) throw Exception("GCM data too short")

        val salt = decoded.sliceArray(0 until SALT_LENGTH)
        val iv = decoded.sliceArray(SALT_LENGTH until SALT_LENGTH + IV_LENGTH_GCM)
        val encrypted = decoded.sliceArray(SALT_LENGTH + IV_LENGTH_GCM until decoded.size)
        
        val key = deriveKey(password, salt, iterations)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_LENGTH, iv))
        
        return String(cipher.doFinal(encrypted))
    }

    private fun performDecryptionCBC(data: String, password: CharArray, iterations: Int): String {
        val decoded = Base64.decode(data, Base64.DEFAULT)
        if (decoded.size < SALT_LENGTH + IV_LENGTH_CBC) throw Exception("CBC data too short")

        val salt = decoded.sliceArray(0 until SALT_LENGTH)
        val iv = decoded.sliceArray(SALT_LENGTH until SALT_LENGTH + IV_LENGTH_CBC)
        val encrypted = decoded.sliceArray(SALT_LENGTH + IV_LENGTH_CBC until decoded.size)
        
        val key = deriveKey(password, salt, iterations)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        
        return String(cipher.doFinal(encrypted))
    }

    private fun deriveKey(password: CharArray, salt: ByteArray, iterations: Int): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, iterations, KEY_LENGTH)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}
