package org.mtier.timetracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import org.mtier.timetracker.data.repository.ServerThemeColors

private val LightColors =
    lightColorScheme(
        primary = NcBlue,
        onPrimary = androidx.compose.ui.graphics.Color.White,
        secondaryContainer = NcBlueLight,
        error = ErrorRed,
    )

private val DarkColors =
    darkColorScheme(
        primary = NcBlueLight,
        onPrimary = NcBlueDark,
        error = ErrorRed,
    )

/**
 * Dynamic color (Android 12+) is opted out on purpose: it would make the
 * app match the *device's* wallpaper-derived palette instead of the
 * Nextcloud server's theme, which defeats the "match the Files app" goal.
 *
 * [serverColors], when available (fetched from the capabilities OCS
 * endpoint — see ThemeRepository), overrides the static Nextcloud-blue
 * fallback with the actual connected server's configured theming color,
 * for both light and dark mode (the capabilities API only exposes one
 * "brand" color + its contrasting text color, not separate light/dark
 * variants, so both schemes reuse the same pair).
 */
@Composable
fun TimeTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    serverColors: ServerThemeColors? = null,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            serverColors != null -> {
                val primary = parseHexColor(serverColors.primaryHex)
                val onPrimary = parseHexColor(serverColors.onPrimaryHex, fallback = androidx.compose.ui.graphics.Color.White)
                if (darkTheme) {
                    darkColorScheme(primary = primary, onPrimary = onPrimary, error = ErrorRed)
                } else {
                    lightColorScheme(primary = primary, onPrimary = onPrimary, error = ErrorRed)
                }
            }
            darkTheme -> DarkColors
            else -> LightColors
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
