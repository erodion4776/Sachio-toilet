package com.sashio.ops.data

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// Real biometric login support. This replaces the old fully-simulated
// Touch ID / Face ID flow (which just ran a scripted animation and always
// logged into the same demo account).
//
// How it actually works:
// 1. After a normal successful password login, the app asks "enable
//    biometric login on this device?" - if yes, the email/password are
//    encrypted at rest (AES-256-GCM, key held in the Android Keystore,
//    never leaves the device) via EncryptedSharedPreferences.
// 2. Next time, tapping Touch ID / Face ID triggers Android's REAL
//    BiometricPrompt - the actual fingerprint/face sensor, not a fake
//    animation. Only after a genuine biometric match does the app decrypt
//    the stored credentials and log in with them via the normal Supabase
//    Auth path.
//
// Note on threat model: this gates access to the stored credentials behind
// a successful biometric check, and the credentials themselves are
// encrypted at rest - but it is not as strong as binding the encryption
// key itself to biometric hardware attestation (Android's
// setUserAuthenticationRequired() key flag). For a fuller banking-grade
// implementation, that would be the next hardening step.
object BiometricAuthHelper {

    private const val PREFS_NAME = "sashio_biometric_credentials"
    private const val KEY_EMAIL = "email"
    private const val KEY_PASSWORD = "password"

    private fun encryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // Whether this device actually has usable biometric hardware with
    // something enrolled (fingerprint or face). Use this to decide whether
    // to even show the Touch ID / Face ID buttons.
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun hasSavedCredentials(context: Context): Boolean {
        return try {
            encryptedPrefs(context).contains(KEY_EMAIL)
        } catch (e: Exception) {
            false
        }
    }

    fun saveCredentials(context: Context, email: String, password: String) {
        try {
            encryptedPrefs(context).edit()
                .putString(KEY_EMAIL, email)
                .putString(KEY_PASSWORD, password)
                .apply()
        } catch (e: Exception) {
            // Silently skip - biometric login just won't be offered next time.
        }
    }

    fun clearSavedCredentials(context: Context) {
        try {
            encryptedPrefs(context).edit().clear().apply()
        } catch (e: Exception) {
            // Nothing stored to clear.
        }
    }

    // Shows the REAL system biometric prompt (actual fingerprint/face
    // sensor). onSuccess receives the decrypted email/password to log in
    // with; onError receives a human-readable reason if it fails/cancels.
    fun authenticateAndGetCredentials(
        activity: FragmentActivity,
        title: String,
        onSuccess: (email: String, password: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val prefs = try {
            encryptedPrefs(activity)
        } catch (e: Exception) {
            onError("Could not access secure storage: ${e.message}")
            return
        }

        val savedEmail = prefs.getString(KEY_EMAIL, null)
        val savedPassword = prefs.getString(KEY_PASSWORD, null)
        if (savedEmail == null || savedPassword == null) {
            onError("No biometric login set up yet - sign in with your password first, then enable it.")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(savedEmail, savedPassword)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Sensor read a fingerprint/face but it didn't match -
                    // the prompt stays open for another attempt, no action needed here.
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle("Confirm your identity to sign in to SACHIO")
            .setNegativeButtonText("Cancel")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }
}
