package org.mtier.timetracker.data.auth

/**
 * Server URL is stored without a trailing slash. [appPassword] is either a
 * device-specific Nextcloud app password (from Login Flow v2) or, in a
 * future v1.x with Files-app SSO, the token vended by the SSO library.
 */
data class Credentials(
    val serverUrl: String,
    val username: String,
    val appPassword: String,
)
