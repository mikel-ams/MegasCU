package com.ams.megascu.security

import android.content.Context
import android.os.SystemClock
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties

/** Stores a verifier for the local PIN, encrypted by a non-exportable Android Keystore key. */
object PinVault {
    private const val KEYSTORE = "AndroidKeyStore"
    private const val KEY_ALIAS = "MegasCU.PinVerifier.v1"
    private const val SECURITY_PREFS = "megas_security_prefs"
    private const val PREF_PIN = "pref_security_pin"
    private const val PREF_FAILED_ATTEMPTS = "pin_failed_attempts"
    private const val PREF_LOCK_EPOCH = "pin_lock_until_epoch_ms"
    private const val PREF_LOCK_ELAPSED = "pin_lock_until_elapsed_ms"
    private const val PREF_BOOT_OFFSET = "pin_lock_boot_offset"
    private const val PBKDF2_ITERATIONS = 120_000
    private const val PBKDF2_KEY_BITS = 256
    private const val GCM_TAG_BITS = 128

    data class LockoutState(val failedAttempts: Int, val remainingMillis: Long)

    fun protect(context: Context, pin: String): String {
        require(pin.matches(Regex("\\d{4,6}"))) { "El PIN debe contener entre 4 y 6 dígitos." }
        val salt = ByteArray(16).also(SecureRandom()::nextBytes)
        val verifier = deriveVerifier(pin, salt)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val ciphertext = cipher.doFinal(verifier)
        return buildString {
            append("v1:")
            append(encode(cipher.iv))
            append(':')
            append(encode(ciphertext))
            append(':')
            append(encode(salt))
        }
    }

    fun verify(context: Context, pin: String, stored: String): Boolean {
        if (stored.isBlank()) return true
        if (!stored.startsWith("v1:")) return stored == pin
        return runCatching {
            val parts = stored.split(':')
            if (parts.size != 4) return false
            val iv = Base64.decode(parts[1], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[2], Base64.NO_WRAP)
            val salt = Base64.decode(parts[3], Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(GCM_TAG_BITS, iv))
            val expected = cipher.doFinal(ciphertext)
            val actual = deriveVerifier(pin, salt)
            MessageDigest.isEqual(expected, actual)
        }.getOrDefault(false)
    }

    fun migrateLegacyPin(context: Context, legacyPrefs: android.content.SharedPreferences): String {
        val securePrefs = securityPrefs(context)
        val secured = securePrefs.getString(PREF_PIN, null).orEmpty()
        if (secured.startsWith("v1:")) return secured

        val legacy = legacyPrefs.getString(PREF_PIN, null).orEmpty()
        if (legacy.isBlank()) return ""
        return if (legacy.matches(Regex("\\d{4,6}"))) {
            runCatching {
                val protected = protect(context, legacy)
                securePrefs.edit().putString(PREF_PIN, protected).apply()
                legacyPrefs.edit().remove(PREF_PIN).apply()
                protected
            }.getOrDefault("")
        } else {
            legacyPrefs.edit().remove(PREF_PIN).apply()
            ""
        }
    }

    fun getStoredPin(context: Context): String =
        securityPrefs(context).getString(PREF_PIN, null).orEmpty()

    fun setPin(context: Context, pin: String) {
        val prefs = securityPrefs(context)
        if (pin.isBlank()) {
            prefs.edit().remove(PREF_PIN).apply()
            clearFailures(context)
            return
        }
        prefs.edit().putString(PREF_PIN, protect(context, pin)).apply()
    }

    fun lockoutState(context: Context): LockoutState {
        val prefs = securityPrefs(context)
        val attempts = prefs.getInt(PREF_FAILED_ATTEMPTS, 0)
        val epochUntil = prefs.getLong(PREF_LOCK_EPOCH, 0L)
        val elapsedUntil = prefs.getLong(PREF_LOCK_ELAPSED, 0L)
        val savedBootOffset = prefs.getLong(PREF_BOOT_OFFSET, Long.MIN_VALUE)
        val currentBootOffset = System.currentTimeMillis() - SystemClock.elapsedRealtime()

        val remaining = if (savedBootOffset != Long.MIN_VALUE && kotlin.math.abs(savedBootOffset - currentBootOffset) > 5_000L) {
            (epochUntil - System.currentTimeMillis()).coerceAtLeast(0L)
        } else {
            maxOf(
                (epochUntil - System.currentTimeMillis()).coerceAtLeast(0L),
                (elapsedUntil - SystemClock.elapsedRealtime()).coerceAtLeast(0L)
            )
        }
        return LockoutState(attempts, remaining)
    }

    fun registerFailedAttempt(context: Context): LockoutState {
        val prefs = securityPrefs(context)
        val attempts = prefs.getInt(PREF_FAILED_ATTEMPTS, 0) + 1
        val wait = when {
            attempts < 3 -> 0L
            attempts < 6 -> 30_000L
            attempts < 10 -> 5 * 60_000L
            else -> 30 * 60_000L
        }
        val nowEpoch = System.currentTimeMillis()
        val nowElapsed = SystemClock.elapsedRealtime()
        prefs.edit()
            .putInt(PREF_FAILED_ATTEMPTS, attempts)
            .putLong(PREF_LOCK_EPOCH, nowEpoch + wait)
            .putLong(PREF_LOCK_ELAPSED, nowElapsed + wait)
            .putLong(PREF_BOOT_OFFSET, nowEpoch - nowElapsed)
            .apply()
        return LockoutState(attempts, wait)
    }

    fun clearFailures(context: Context) {
        securityPrefs(context).edit()
            .remove(PREF_FAILED_ATTEMPTS)
            .remove(PREF_LOCK_EPOCH)
            .remove(PREF_LOCK_ELAPSED)
            .remove(PREF_BOOT_OFFSET)
            .apply()
    }

    private fun securityPrefs(context: Context): android.content.SharedPreferences =
        context.getSharedPreferences(SECURITY_PREFS, Context.MODE_PRIVATE)

    private fun deriveVerifier(pin: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(pin.toCharArray(), salt, PBKDF2_ITERATIONS, PBKDF2_KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun getOrCreateKey(): java.security.Key {
        val keyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        keyStore.getKey(KEY_ALIAS, null)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private fun encode(value: ByteArray): String = Base64.encodeToString(value, Base64.NO_WRAP)
}
