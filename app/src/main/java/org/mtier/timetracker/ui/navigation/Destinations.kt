package org.mtier.timetracker.ui.navigation

sealed class Destination(
    val route: String,
) {
    data object Login : Destination("login")

    data object Timer : Destination("timer")

    data object Dashboard : Destination("dashboard")

    data object Goals : Destination("goals")

    data object Reports : Destination("reports")

    data object Timelines : Destination("timelines")

    data object TimelinesAdmin : Destination("timelines-admin")

    data object Projects : Destination("projects")

    data object Clients : Destination("clients")

    data object Tags : Destination("tags")
}

/** Drawer order matches the current web sidebar (router.js) — TimelinesAdmin
 *  is filtered out for non-admins by the caller (see MainScaffold), same as
 *  the web frontend's App.vue `views.filter(... || this.isAdmin)`. */
val drawerDestinations =
    listOf(
        Destination.Timer,
        Destination.Dashboard,
        Destination.Goals,
        Destination.Reports,
        Destination.Timelines,
        Destination.TimelinesAdmin,
        Destination.Projects,
        Destination.Clients,
        Destination.Tags,
    )
