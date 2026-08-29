package com.sashio.ops.ui

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object BiometricAuthHelper {
    private const val PREFS_NAME = "sashio_secure_bio_prefs"
    private const val KEY_SAVED_EMAIL = "bio_saved_email"
    private const val KEY_SAVED_PASSWORD = "bio_saved_password"

    private fun getSecurePrefs(context: Context) = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Checks if the device has biometric hardware (Fingerprint / Face) configured and ready.
     */
    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Checks if user credentials have been securely stored after at least one successful manual password sign-in.
     */
    fun hasSavedCredentials(context: Context): Boolean {
        val prefs = getSecurePrefs(context)
        val email = prefs.getString(KEY_SAVED_EMAIL, null)
        val pass = prefs.getString(KEY_SAVED_PASSWORD, null)
        return !email.isNullOrBlank() && !pass.isNullOrBlank()
    }

    /**
     * Stores the user credentials encrypted on device.
     */
    fun saveCredentials(context: Context, email: String, pass: String) {
        getSecurePrefs(context).edit()
            .putString(KEY_SAVED_EMAIL, email.trim().lowercase())
            .putString(KEY_SAVED_PASSWORD, pass)
            .apply()
    }

    /**
     * Clears biometric login credentials (e.g., upon logout / account deletion).
     */
    fun clearSavedCredentials(context: Context) {
        getSecurePrefs(context).edit().clear().apply()
    }

    /**
     * Launches the system biometric prompt and retrieves saved credentials on success.
     */
    fun authenticateAndGetCredentials(
        activity: FragmentActivity,
        title: String = "Biometric Sign-In",
        subtitle: String = "Authenticate with biometric sensor to access your SACHIO portal",
        onSuccess: (savedEmail: String, savedPass: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val prefs = getSecurePrefs(activity)
        val email = prefs.getString(KEY_SAVED_EMAIL, null)
        val pass = prefs.getString(KEY_SAVED_PASSWORD, null)

        if (email.isNullOrBlank() || pass.isNullOrBlank()) {
            onError("No biometric credentials saved. Please sign in with your email and password once first.")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use Password")
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess(email, pass)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    // errorCode 13 (Cancel by user) or 10 (Negative button)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        onError(errString.toString())
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Biometric authentication failed. Please try again.")
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
