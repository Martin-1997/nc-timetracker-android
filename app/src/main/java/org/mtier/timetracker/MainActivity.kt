package org.mtier.timetracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import dagger.hilt.android.AndroidEntryPoint
import org.mtier.timetracker.data.auth.AuthRepository
import org.mtier.timetracker.data.auth.NextcloudSsoManager
import org.mtier.timetracker.data.repository.ThemeRepository
import org.mtier.timetracker.ui.navigation.TimeTrackerNavHost
import org.mtier.timetracker.ui.theme.TimeTrackerTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var themeRepository: ThemeRepository

    @Inject
    lateinit var ssoManager: NextcloudSsoManager

    /** The Android-SingleSignOn library's account chooser is still
     *  startActivityForResult-based, so its result only reaches the app
     *  through these two legacy callbacks — see NextcloudSsoManager's kdoc. */
    @Deprecated("Required by com.github.nextcloud:Android-SingleSignOn's AccountImporter API")
    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        ssoManager.handleActivityResult(requestCode, resultCode, data, this)
    }

    @Deprecated("Required by com.github.nextcloud:Android-SingleSignOn's AccountImporter API")
    @Suppress("DEPRECATION")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        ssoManager.handlePermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isLoggedIn = authRepository.currentCredentials() != null
        setContent {
            // Also re-triggered right after a fresh login (LoginViewModel),
            // so the server's theme applies immediately either way — this
            // covers the cold-start-while-already-authenticated case.
            LaunchedEffect(isLoggedIn) {
                if (isLoggedIn) themeRepository.refresh()
            }
            val serverColors by themeRepository.colors.collectAsState()

            TimeTrackerTheme(serverColors = serverColors) {
                TimeTrackerNavHost(isLoggedIn = isLoggedIn)
            }
        }
    }
}
