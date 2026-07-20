package org.mtier.timetracker.data.repository

import org.mtier.timetracker.data.api.OcsApi
import javax.inject.Inject
import javax.inject.Singleton

data class NextcloudUser(
    val uid: String,
    val displayName: String,
)

private const val ADMIN_GROUP = "admin"

@Singleton
class UsersRepository
    @Inject
    constructor(
        private val api: OcsApi,
    ) {
        /** Locked-project fields (allowedTags/allowedUsers) are only ever
         *  honored server-side for members of the "admin" group — see
         *  AjaxController::editProject's isThisAdminUser() checks. */
        suspend fun isCurrentUserAdmin(): Boolean = api.getSelfUser().ocs.data.groups.contains(ADMIN_GROUP)

        suspend fun getUsers(): List<NextcloudUser> =
            api
                .getUsersDetails()
                .ocs.data.users.values
                .map { NextcloudUser(uid = it.id, displayName = it.displayname?.ifBlank { null } ?: it.id) }
                .sortedBy { it.displayName.lowercase() }
    }
