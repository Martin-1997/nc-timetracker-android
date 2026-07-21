package org.mtier.timetracker.data.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Extracted so AuthRepository can be constructed in a plain JVM unit test
 *  against an in-memory fake — EncryptedSharedPreferences needs a real
 *  Android Context, which isn't available there. */
interface CredentialStorage {
    fun save(credentials: Credentials)

    fun load(): Credentials?

    fun clear()
}

/**
 * Keystore-backed storage for the single account V1 supports. No plaintext
 * ever touches disk; the master key itself lives in the Android Keystore,
 * which works identically on GrapheneOS (no Play Services involved).
 */
@Singleton
class CredentialStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : CredentialStorage {
        private val masterKey =
            MasterKey
                .Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

        private val prefs =
            EncryptedSharedPreferences.create(
                context,
                PREFS_FILE,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )

        override fun save(credentials: Credentials) {
            prefs
                .edit()
                .putString(KEY_SERVER_URL, credentials.serverUrl)
                .putString(KEY_USERNAME, credentials.username)
                .putString(KEY_APP_PASSWORD, credentials.appPassword)
                .apply()
        }

        override fun load(): Credentials? {
            val serverUrl = prefs.getString(KEY_SERVER_URL, null) ?: return null
            val username = prefs.getString(KEY_USERNAME, null) ?: return null
            val appPassword = prefs.getString(KEY_APP_PASSWORD, null) ?: return null
            return Credentials(serverUrl, username, appPassword)
        }

        override fun clear() {
            prefs.edit().clear().apply()
        }

        private companion object {
            const val PREFS_FILE = "timetracker_credentials"
            const val KEY_SERVER_URL = "server_url"
            const val KEY_USERNAME = "username"
            const val KEY_APP_PASSWORD = "app_password"
        }
    }
