package com.example.allinone.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class BiometricAuthHelper(
    private val activity: FragmentActivity,
    private val onAuthSuccess: () -> Unit,
    private val onAuthError: (String) -> Unit
) {
    private val executor: Executor = ContextCompat.getMainExecutor(activity)
    
    private val biometricPrompt = BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                onAuthError(errString.toString())
            }
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onAuthSuccess()
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
        }
    })

    private val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Login")
        .setSubtitle("Unlock using your biometric credential")
        .setNegativeButtonText("Use PIN")
        .build()

    fun showBiometricPrompt() {
        val biometricManager = BiometricManager.from(activity)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
            biometricPrompt.authenticate(promptInfo)
        }
    }
}
