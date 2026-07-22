package org.mtier.timetracker.data.auth

/**
 * Server URL is stored without a trailing slash.
 *
 * [appPassword] is a device-specific Nextcloud app password when
 * [isSso] is false (from Login Flow v2) — usable directly as an HTTP Basic
 * Auth password. When [isSso] is true, it's the token vended by the
 * Android-SingleSignOn library instead, which is NOT a usable server
 * credential on its own (confirmed against the library's own source: the
 * Nextcloud Files app matches it against the calling app's package
 * namespace and performs the actual authenticated request itself, in its
 * own process, over AIDL) — [org.mtier.timetracker.data.network.AuthInterceptor]
 * routes requests through [org.mtier.timetracker.data.network.SsoNetworkClient]
 * instead of Basic Auth whenever this is true.
 */
data class Credentials(
    val serverUrl: String,
    val username: String,
    val appPassword: String,
    val isSso: Boolean = false,
)
