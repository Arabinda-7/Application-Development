package com.example.allinone.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.SecureRandom
import android.view.WindowManager
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object SecurityManager {
    private const val ENCRYPTED_PREFS_NAME = "all_in_one_secure_prefs"
    private const val KEY_DB_PASSPHRASE = "db_passphrase"

    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    private const val ITERATION_COUNT = 10000
    private const val KEY_LENGTH = 256

    fun getEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getDatabasePassphrase(context: Context): String {
        val prefs = getEncryptedPrefs(context)
        var passphrase = prefs.getString(KEY_DB_PASSPHRASE, null)
        if (passphrase == null) {
            passphrase = generateSecurePassphrase()
            prefs.edit().putString(KEY_DB_PASSPHRASE, passphrase).apply()
        }
        return passphrase
    }

    private fun generateSecurePassphrase(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
    }
    
    fun setScreenshotProtection(activity: android.app.Activity, enabled: Boolean) {
        if (enabled) {
            activity.window.setFlags(
                android.view.WindowManager.LayoutParams.FLAG_SECURE,
                android.view.WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            activity.window.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    fun encryptData(data: String, password: CharArray): String {
        val salt = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        val iv = ByteArray(16).apply { SecureRandom().nextBytes(this) }
        
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        
        val encrypted = cipher.doFinal(data.toByteArray())
        
        val combined = salt + iv + encrypted
        return android.util.Base64.encodeToString(combined, android.util.Base64.NO_WRAP)
    }

    fun decryptData(encryptedData: String, password: CharArray): String {
        val combined = android.util.Base64.decode(encryptedData, android.util.Base64.NO_WRAP)
        
        val salt = combined.sliceArray(0 until 16)
        val iv = combined.sliceArray(16 until 32)
        val encrypted = combined.sliceArray(32 until combined.size)
        
        val key = deriveKey(password, salt)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        
        return String(cipher.doFinal(encrypted))
    }

    private fun deriveKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH)
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }
}
