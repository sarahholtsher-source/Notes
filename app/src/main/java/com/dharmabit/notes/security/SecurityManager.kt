package com.dharmabit.notes.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class SecurityManager(private val context: Context) {
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("security_prefs", Context.MODE_PRIVATE)
    private val keyAlias = "notes_encryption_key"

    companion object {
        const val PREF_SECURITY_ENABLED = "security_enabled"
        const val PREF_PIN_HASH = "pin_hash"
        const val PREF_USE_BIOMETRIC = "use_biometric"
        const val PREF_PRIVATE_NOTES_ENABLED = "private_notes_enabled"
    }

    fun isSecurityEnabled(): Boolean = sharedPrefs.getBoolean(PREF_SECURITY_ENABLED, false)

    fun enableSecurity(pin: String, useBiometric: Boolean) {
        val hashedPin = hashPin(pin)
        android.util.Log.d("SecurityManager", "Enabling security with PIN hash: $hashedPin")

        sharedPrefs.edit()
            .putBoolean(PREF_SECURITY_ENABLED, true)
            .putString(PREF_PIN_HASH, hashedPin)
            .putBoolean(PREF_USE_BIOMETRIC, useBiometric)
            .apply()
        generateEncryptionKey()
    }

    fun disableSecurity() {
        sharedPrefs.edit()
            .putBoolean(PREF_SECURITY_ENABLED, false)
            .remove(PREF_PIN_HASH)
            .putBoolean(PREF_USE_BIOMETRIC, false)
            .putBoolean(PREF_PRIVATE_NOTES_ENABLED, false)
            .apply()
        deleteEncryptionKey()
    }

    fun validatePin(pin: String): Boolean {
        val storedHash = sharedPrefs.getString(PREF_PIN_HASH, "") ?: ""
        val inputHash = hashPin(pin)

        android.util.Log.d("SecurityManager", "Stored hash: $storedHash")
        android.util.Log.d("SecurityManager", "Input hash: $inputHash")
        android.util.Log.d("SecurityManager", "Match: ${inputHash == storedHash}")

        return inputHash == storedHash
    }

    fun isBiometricEnabled(): Boolean = sharedPrefs.getBoolean(PREF_USE_BIOMETRIC, false)

    fun canUseBiometric(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun authenticateWithBiometric(
        activity: FragmentActivity,
        title: String = "Unlock Notes",
        subtitle: String = "Use your fingerprint to unlock",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!canUseBiometric()) {
            onError("Biometric authentication not available")
            return
        }

        val executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt =
            BiometricPrompt(activity, executor, object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed")
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun hashPin(pin: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } catch (e: Exception) {
            android.util.Log.e("SecurityManager", "Error hashing PIN", e)
            pin.hashCode().toString()
        }
    }

    private fun generateEncryptionKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun deleteEncryptionKey() {
        try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)
            keyStore.deleteEntry(keyAlias)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun encryptText(plainText: String): String? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val encryptedBytes = cipher.doFinal(plainText.toByteArray())
            val iv = cipher.iv

            val combined = iv + encryptedBytes
            Base64.encodeToString(combined, Base64.DEFAULT)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decryptText(encryptedText: String): String? {
        return try {
            val keyStore = KeyStore.getInstance("AndroidKeyStore")
            keyStore.load(null)

            val secretKey = keyStore.getKey(keyAlias, null) as SecretKey
            val combined = Base64.decode(encryptedText, Base64.DEFAULT)

            val iv = combined.sliceArray(0..11) // GCM IV is 12 bytes
            val encryptedBytes = combined.sliceArray(12 until combined.size)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun isPrivateNotesEnabled(): Boolean = sharedPrefs.getBoolean(PREF_PRIVATE_NOTES_ENABLED, false)

    fun enablePrivateNotes() {
        sharedPrefs.edit().putBoolean(PREF_PRIVATE_NOTES_ENABLED, true).apply()
    }

    fun disablePrivateNotes() {
        sharedPrefs.edit().putBoolean(PREF_PRIVATE_NOTES_ENABLED, false).apply()
    }

    fun resetSecurity() {
        sharedPrefs.edit().clear().apply()
        deleteEncryptionKey()
    }
}