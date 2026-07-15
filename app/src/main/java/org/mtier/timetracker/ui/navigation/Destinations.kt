package org.mtier.timetracker.ui.navigation

sealed class Destination(
    val route: String,
) {
    data object Login : Destination("login")

    data object Timer : Destination("timer")

    data object Projects : Destination("projects")

    data object Clients : Destination("clients")

    data object Tags : Destination("tags")
}

/** Drawer order matches the current web sidebar for the views in MVP scope
 *  (see PLAN.md §5) — Dashboard/Goals/Reports/Timelines slot in here once
 *  their v1.x releases land, without restructuring anything else. */
val drawerDestinations = listOf(Destination.Timer, Destination.Projects, Destination.Clients, Destination.Tags)
