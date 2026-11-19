package org.mtier.timetracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.mtier.timetracker.data.auth.AuthRepository
import org.mtier.timetracker.data.auth.NextcloudSsoManager
import org.mtier.timetracker.data.repository.ServerCapabilitiesRepository
import org.mtier.timetracker.ui.navigation.TimeTrackerNavHost
import org.mtier.timetracker.ui.theme.TimeTrackerTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var capabilitiesRepository: ServerCapabilitiesRepository

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

    /**
     * Re-reads the server capabilities (theme colors, timetracker version and
     * features) on cold start and every return to the foreground, so a server
     * update is noticed without signing in again. A fresh login triggers its
     * own refresh (LoginViewModel).
     */
    override fun onStart() {
        super.onStart()
        if (authRepository.currentCredentials() != null) {
            lifecycleScope.launch { capabilitiesRepository.refresh() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val isLoggedIn = authRepository.currentCredentials() != null
        setContent {
            val serverColors by capabilitiesRepository.colors.collectAsState()

            TimeTrackerTheme(serverColors = serverColors) {
                TimeTrackerNavHost(isLoggedIn = isLoggedIn)
            }
        }
    }
}
