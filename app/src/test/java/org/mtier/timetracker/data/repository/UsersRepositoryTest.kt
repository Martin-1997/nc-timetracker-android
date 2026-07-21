package org.mtier.timetracker.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mtier.timetracker.data.api.dto.OcsSelfUserDto
import org.mtier.timetracker.data.api.dto.OcsUserDetailDto
import org.mtier.timetracker.data.api.dto.OcsUsersDetailsDto
import org.mtier.timetracker.fakes.FakeOcsApi

class UsersRepositoryTest {
    private val api = FakeOcsApi()
    private val repository = UsersRepository(api)

    @Test
    fun `isCurrentUserAdmin is true when the self user's groups include admin`() =
        runTest {
            api.selfUser = OcsSelfUserDto(id = "alice", groups = listOf("users", "admin"))

            assertTrue(repository.isCurrentUserAdmin())
        }

    @Test
    fun `isCurrentUserAdmin is false for a non-admin user`() =
        runTest {
            api.selfUser = OcsSelfUserDto(id = "bob", groups = listOf("users"))

            assertFalse(repository.isCurrentUserAdmin())
        }

    @Test
    fun `getUsers falls back to uid when displayname is blank and sorts case-insensitively`() =
        runTest {
            api.usersDetails =
                OcsUsersDetailsDto(
                    users =
                        mapOf(
                            "carol" to OcsUserDetailDto(id = "carol", displayname = "  "),
                            "alice" to OcsUserDetailDto(id = "alice", displayname = "Alice Smith"),
                            "bob" to OcsUserDetailDto(id = "bob", displayname = "bob jones"),
                        ),
                )

            val users = repository.getUsers()

            assertEquals(
                listOf(
                    NextcloudUser("alice", "Alice Smith"),
                    NextcloudUser("bob", "bob jones"),
                    NextcloudUser("carol", "carol"),
                ),
                users,
            )
        }
}
