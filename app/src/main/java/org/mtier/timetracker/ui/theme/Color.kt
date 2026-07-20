package org.mtier.timetracker.ui.theme

import androidx.compose.ui.graphics.Color

// Nextcloud's default theming primary color (#0082c9), matching the Files
// app — used as the fallback until the server's own theming color (fetched
// from the capabilities API, see ThemeRepository) is available.
val NcBlue = Color(0xFF0082C9)
val NcBlueDark = Color(0xFF006AA3)
val NcBlueLight = Color(0xFFD8ECF9)

val ErrorRed = Color(0xFFE9322D)
val SuccessGreen = Color(0xFF46BA61)

fun parseHexColor(
    hex: String?,
    fallback: Color = NcBlue,
): Color {
    if (hex == null) return fallback
    return runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(fallback)
}
