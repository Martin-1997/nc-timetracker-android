package org.mtier.timetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import org.mtier.timetracker.data.auth.AuthRepository
import org.mtier.timetracker.ui.navigation.TimeTrackerNavHost
import org.mtier.timetracker.ui.theme.TimeTrackerTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimeTrackerTheme {
                TimeTrackerNavHost(
                    isLoggedIn = authRepository.currentCredentials() != null,
                )
            }
        }
    }
}
