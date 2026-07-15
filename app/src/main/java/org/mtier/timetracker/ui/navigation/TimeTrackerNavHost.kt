package org.mtier.timetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.mtier.timetracker.R
import org.mtier.timetracker.ui.clients.ClientsScreen
import org.mtier.timetracker.ui.login.LoginScreen
import org.mtier.timetracker.ui.main.MainScaffold
import org.mtier.timetracker.ui.main.SessionViewModel
import org.mtier.timetracker.ui.projects.ProjectsScreen
import org.mtier.timetracker.ui.tags.TagsScreen
import org.mtier.timetracker.ui.timer.TimerScreen

@Composable
fun TimeTrackerNavHost(isLoggedIn: Boolean) {
    val navController = rememberNavController()
    val sessionViewModel: SessionViewModel = hiltViewModel()

    fun goToDestination(destination: Destination) {
        navController.navigate(destination.route) {
            popUpTo(Destination.Timer.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun onSignedOut() {
        sessionViewModel.signOut()
        navController.navigate(Destination.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Destination.Timer.route else Destination.Login.route,
    ) {
        composable(Destination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Destination.Timer.route) {
                        popUpTo(Destination.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Destination.Timer.route) {
            MainScaffold(
                currentDestination = Destination.Timer,
                title = stringResource(R.string.nav_timer),
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> TimerScreen(modifier = modifier) }
        }

        composable(Destination.Projects.route) {
            MainScaffold(
                currentDestination = Destination.Projects,
                title = stringResource(R.string.nav_projects),
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> ProjectsScreen(modifier = modifier) }
        }

        composable(Destination.Clients.route) {
            MainScaffold(
                currentDestination = Destination.Clients,
                title = stringResource(R.string.nav_clients),
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> ClientsScreen(modifier = modifier) }
        }

        composable(Destination.Tags.route) {
            MainScaffold(
                currentDestination = Destination.Tags,
                title = stringResource(R.string.nav_tags),
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> TagsScreen(modifier = modifier) }
        }
    }
}
