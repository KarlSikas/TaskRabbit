package com.example.taskrabbit.data.security // Adjust package if you place it elsewhere

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log // Keep Log import for error logging
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import java.lang.IllegalArgumentException // Import explicitly

object EncryptionHandler {

    private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
    private const val KEY_ALIAS = "TaskRabbitDbEncryptionKey_v1"
    private const val PREFS_FILE = "TaskRabbitSecurePrefs_v1"
    private const val ENCRYPTED_PASSPHRASE_KEY = "db_pass_k1"
    private const val ENCRYPTION_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_SEPARATOR = "]"
    private const val ANDROID_KEYSTORE_TAG_LENGTH = 128
    private const val PASSPHRASE_SIZE_BYTES = 32
    private const val TAG = "EncryptionHandler" // Keep TAG for error logs

    /**
     * Retrieves the decrypted database passphrase bytes.
     * If no passphrase exists, it generates, encrypts, and stores a new one.
     * Handles decryption and uses Android Keystore for cryptographic operations.
     *
     * @param context The application context.
     * @return A ByteArray containing the raw passphrase for SQLCipher.
     * @throws SecurityException if Keystore operations fail or decryption is unsuccessful,
     *         indicating a potential issue with the Keystore key or stored data.
     */
    @Throws(SecurityException::class)
    fun getDatabasePassphraseBytes(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        val encryptedData = prefs.getString(ENCRYPTED_PASSPHRASE_KEY, null)

        return if (encryptedData != null) {
            // Log removed: "Found existing encrypted passphrase. Attempting decryption."
            try {
                decrypt(encryptedData)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decrypt existing passphrase! Keystore key might be invalid or data corrupted.", e) // Keep error log
                throw SecurityException("Failed to decrypt database passphrase. Potential data loss or key invalidation.", e)
            }
        } else {
            // Log removed: "No existing passphrase found. Generating and storing a new one."
            generateAndStoreEncryptedPassphrase(context)
        }
    }

    @Throws(SecurityException::class)
    private fun generateAndStoreEncryptedPassphrase(context: Context): ByteArray {
        val passphraseBytes = ByteArray(PASSPHRASE_SIZE_BYTES).apply { SecureRandom().nextBytes(this) }
        // Log removed: "Generated ${passphraseBytes.size * 8}-bit random passphrase."

        val encryptedData = try {
            encrypt(passphraseBytes)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to encrypt the new passphrase.", e) // Keep error log
            throw SecurityException("Could not encrypt database passphrase during initial setup.", e)
        }

        val prefs = context.getSharedPreferences(PREFS_FILE, Context.MODE_PRIVATE)
        prefs.edit().putString(ENCRYPTED_PASSPHRASE_KEY, encryptedData).apply()
        // Log removed: "Stored new encrypted passphrase in SharedPreferences."

        return passphraseBytes
    }

    // --- Keystore and Cryptographic Operations ---

    @Throws(Exception::class)
    private fun getKeyStore(): KeyStore {
        return KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
    }

    /**
     * Retrieves the SecretKey from Android Keystore or generates a new one if it doesn't exist.
     */
    @Throws(Exception::class)
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = getKeyStore()
        val existingKey = keyStore.getKey(KEY_ALIAS, null)

        if (existingKey is SecretKey) {
            // Log removed: "Successfully retrieved existing Keystore key with alias: $KEY_ALIAS"
            return existingKey
        }

        // Log removed: "Keystore key '$KEY_ALIAS' not found. Generating a new key."

        val keyGenSpecBuilder = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)

        // Optional security constraints remain commented out

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )
        keyGenerator.init(keyGenSpecBuilder.build())

        val newKey = keyGenerator.generateKey()
        // Log removed: "Successfully generated and stored new Keystore key with alias: $KEY_ALIAS"
        return newKey
    }

    /**
     * Encrypts the given data using AES/GCM with a key from Android Keystore.
     * Returns a Base64 encoded string containing "[IV]ciphertext".
     */
    @Throws(Exception::class)
    private fun encrypt(data: ByteArray): String {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)

        val iv = cipher.iv
        if (iv == null || iv.isEmpty()) {
            throw SecurityException("IV cannot be null or empty for GCM mode")
        }
        // Log removed: "Generated IV of size: ${iv.size} bytes"

        val encryptedBytes = cipher.doFinal(data)

        val ivString = Base64.encodeToString(iv, Base64.NO_WRAP)
        val encryptedString = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)

        val combined = ivString + IV_SEPARATOR + encryptedString
        // Log removed: "Encryption successful. Combined length: ${combined.length}"
        return combined
    }

    /**
     * Decrypts the "[IV]ciphertext" string using AES/GCM with a key from Android Keystore.
     * Returns the original plaintext data as a ByteArray.
     */
    @Throws(Exception::class)
    private fun decrypt(encryptedDataString: String): ByteArray {
        val parts = encryptedDataString.split(IV_SEPARATOR)
        if (parts.size != 2) {
            Log.e(TAG, "Invalid encrypted data format. Expected '[IV]$IV_SEPARATOR[Ciphertext]'.") // Keep error log
            throw IllegalArgumentException("Invalid encrypted data format")
        }

        val ivString = parts[0]
        val encryptedString = parts[1]
        val iv = Base64.decode(ivString, Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(encryptedString, Base64.NO_WRAP)
        // Log removed: "Decoding IV size: ${iv.size}, Ciphertext size: ${encryptedBytes.size}"


        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
        val spec = GCMParameterSpec(ANDROID_KEYSTORE_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val decryptedBytes = cipher.doFinal(encryptedBytes)
        // Log removed: "Decryption successful. Plaintext size: ${decryptedBytes.size}"
        return decryptedBytes
    }
}