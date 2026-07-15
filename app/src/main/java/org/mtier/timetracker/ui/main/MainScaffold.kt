package org.mtier.timetracker.ui.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.mtier.timetracker.R
import org.mtier.timetracker.ui.navigation.Destination
import org.mtier.timetracker.ui.navigation.drawerDestinations

private fun iconFor(destination: Destination): ImageVector =
    when (destination) {
        Destination.Timer -> Icons.Filled.Timer
        Destination.Projects -> Icons.Filled.Work
        Destination.Clients -> Icons.Filled.Group
        Destination.Tags -> Icons.Filled.Label
        else -> Icons.Filled.AttachMoney
    }

private fun labelFor(destination: Destination): Int =
    when (destination) {
        Destination.Timer -> R.string.nav_timer
        Destination.Projects -> R.string.nav_projects
        Destination.Clients -> R.string.nav_clients
        Destination.Tags -> R.string.nav_tags
        else -> R.string.nav_timer
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScaffold(
    currentDestination: Destination,
    title: String,
    onNavigate: (Destination) -> Unit,
    onSignOut: () -> Unit,
    content: @Composable (Modifier) -> Unit,
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                drawerDestinations.forEach { destination ->
                    NavigationDrawerItem(
                        icon = { Icon(iconFor(destination), contentDescription = null) },
                        label = { Text(stringResource(labelFor(destination))) },
                        selected = destination == currentDestination,
                        onClick = {
                            scope.launch { drawerState.close() }
                            onNavigate(destination)
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_sign_out)) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        onSignOut()
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = null)
                        }
                    },
                    colors =
                        androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            titleContentColor = MaterialTheme.colorScheme.onPrimary,
                            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                )
            },
        ) { innerPadding ->
            content(Modifier.padding(innerPadding))
        }
    }
}
