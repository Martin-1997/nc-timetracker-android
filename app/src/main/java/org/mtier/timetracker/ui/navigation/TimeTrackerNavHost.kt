package org.mtier.timetracker.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.mtier.timetracker.R
import org.mtier.timetracker.ui.clients.ClientsScreen
import org.mtier.timetracker.ui.dashboard.DashboardScreen
import org.mtier.timetracker.ui.goals.GoalsScreen
import org.mtier.timetracker.ui.login.LoginScreen
import org.mtier.timetracker.ui.main.MainScaffold
import org.mtier.timetracker.ui.main.SessionViewModel
import org.mtier.timetracker.ui.projects.ProjectsScreen
import org.mtier.timetracker.ui.reports.ReportsScreen
import org.mtier.timetracker.ui.tags.TagsScreen
import org.mtier.timetracker.ui.timelines.TimelinesScreen
import org.mtier.timetracker.ui.timelinesadmin.TimelinesAdminScreen
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
        sessionViewModel.signOut {
            navController.navigate(Destination.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) Destination.Timer.route else Destination.Login.route,
    ) {
        composable(Destination.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    // The pre-login check in SessionViewModel's init always
                    // resolves to false (no authenticated session yet).
                    sessionViewModel.refreshIsAdmin()
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
                isAdmin = sessionViewModel.isAdmin,
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> TimerScreen(modifier = modifier) }
        }

        composable(Destination.Dashboard.route) {
            MainScaffold(
                currentDestination = Destination.Dashboard,
                title = stringResource(R.string.nav_dashboard),
                isAdmin = sessionViewModel.isAdmin,
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> DashboardScreen(modifier = modifier) }
        }

        composable(Destination.Goals.route) {
            MainScaffold(
                currentDestination = Destination.Goals,
                title = stringResource(R.string.nav_goals),
                isAdmin = sessionViewModel.isAdmin,
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> GoalsScreen(modifier = modifier) }
        }

        composable(Destination.Reports.route) {
            MainScaffold(
                currentDestination = Destination.Reports,
                title = stringResource(R.string.nav_reports),
                isAdmin = sessionViewModel.isAdmin,
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> ReportsScreen(modifier = modifier) }
        }

        composable(Destination.Timelines.route) {
            MainScaffold(
                currentDestination = Destination.Timelines,
                title = stringResource(R.string.nav_timelines),
                isAdmin = sessionViewModel.isAdmin,
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> TimelinesScreen(modifier = modifier) }
        }

        composable(Destination.TimelinesAdmin.route) {
            MainScaffold(
                currentDestination = Destination.TimelinesAdmin,
                title = stringResource(R.string.nav_timelines_admin),
                isAdmin = sessionViewModel.isAdmin,
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> TimelinesAdminScreen(modifier = modifier) }
        }

        composable(Destination.Projects.route) {
            MainScaffold(
                currentDestination = Destination.Projects,
                title = stringResource(R.string.nav_projects),
                isAdmin = sessionViewModel.isAdmin,
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> ProjectsScreen(modifier = modifier) }
        }

        composable(Destination.Clients.route) {
            MainScaffold(
                currentDestination = Destination.Clients,
                title = stringResource(R.string.nav_clients),
                isAdmin = sessionViewModel.isAdmin,
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> ClientsScreen(modifier = modifier) }
        }

        composable(Destination.Tags.route) {
            MainScaffold(
                currentDestination = Destination.Tags,
                title = stringResource(R.string.nav_tags),
                isAdmin = sessionViewModel.isAdmin,
                onNavigate = ::goToDestination,
                onSignOut = ::onSignedOut,
            ) { modifier -> TagsScreen(modifier = modifier) }
        }
    }
}
