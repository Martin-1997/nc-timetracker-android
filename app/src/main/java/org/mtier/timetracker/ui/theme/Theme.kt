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

private val LightColors = lightColorScheme(
    primary = NcBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondaryContainer = NcBlueLight,
    error = ErrorRed,
)

private val DarkColors = darkColorScheme(
    primary = NcBlueLight,
    onPrimary = NcBlueDark,
    error = ErrorRed,
)

/**
 * Dynamic color (Android 12+) is opted out on purpose: it would make the
 * app match the *device's* wallpaper-derived palette instead of the
 * Nextcloud server's theme, which defeats the "match the Files app" goal.
 * A future release fetches the server's actual theming colors from the
 * capabilities OCS endpoint instead (PLAN.md §5); for now every device
 * gets the same Nextcloud-blue palette regardless of Android version.
 */
@Composable
fun TimeTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
