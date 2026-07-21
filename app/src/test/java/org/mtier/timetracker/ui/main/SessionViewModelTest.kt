@file:OptIn(ExperimentalCoroutinesApi::class)

package org.mtier.timetracker.ui.main

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mtier.timetracker.MainDispatcherRule
import org.mtier.timetracker.data.auth.AuthRepository
import org.mtier.timetracker.data.repository.UsersRepository
import org.mtier.timetracker.fakes.FakeCacheStore
import org.mtier.timetracker.fakes.FakeCredentialStore
import org.mtier.timetracker.fakes.FakeOcsApi
import org.mtier.timetracker.fakes.FakeSsoAccountManager

class SessionViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val credentialStore = FakeCredentialStore()
    private val cacheStore = FakeCacheStore()
    private val ssoAccountManager = FakeSsoAccountManager()

    private fun viewModel(): SessionViewModel =
        SessionViewModel(
            AuthRepository(credentialStore, ssoAccountManager, cacheStore),
            UsersRepository(FakeOcsApi()),
        )

    @Test
    fun `signOut only calls onComplete after AuthRepository signOut finishes`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()
            var completed = false

            viewModel.signOut { completed = true }
            assertTrue(!completed)

            advanceUntilIdle()

            assertTrue(completed)
            assertTrue(cacheStore.cleared)
            assertTrue(ssoAccountManager.cleared)
        }

    @Test
    fun `signOut still calls onComplete if AuthRepository signOut throws`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // cacheStore failures are swallowed inside AuthRepository itself
            // (best-effort); ssoManager.clearAccount() isn't, so it's the one
            // that actually reaches SessionViewModel's own catch block.
            ssoAccountManager.clearError = java.io.IOException("SSO library write failed")
            val viewModel = viewModel()
            advanceUntilIdle()
            var completed = false

            viewModel.signOut { completed = true }
            advanceUntilIdle()

            assertTrue(completed)
        }
}
