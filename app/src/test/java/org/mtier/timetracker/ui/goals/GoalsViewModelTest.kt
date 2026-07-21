@file:OptIn(ExperimentalCoroutinesApi::class)

package org.mtier.timetracker.ui.goals

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mtier.timetracker.MainDispatcherRule
import org.mtier.timetracker.data.api.dto.GoalDto
import org.mtier.timetracker.data.api.dto.GoalsResponse
import org.mtier.timetracker.data.repository.GoalsRepository
import org.mtier.timetracker.data.repository.ProjectsRepository
import org.mtier.timetracker.fakes.FakeProjectsCache
import org.mtier.timetracker.fakes.FakeTimeTrackerApi

class GoalsViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeTimeTrackerApi()

    private fun viewModel(): GoalsViewModel = GoalsViewModel(GoalsRepository(api), ProjectsRepository(api, FakeProjectsCache()))

    @Test
    fun `refresh populates goals from the repository`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val goal = GoalDto(id = 1, projectId = 5, hours = 10, interval = "Weekly")
            api.goalsResponse = GoalsResponse(listOf(goal))

            val viewModel = viewModel()
            advanceUntilIdle()

            assertEquals(listOf(goal), viewModel.uiState.goals)
            assertTrue(!viewModel.uiState.isLoading)
        }

    @Test
    fun `addGoal is a no-op when hours is blank`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onNewGoalHoursChanged("   ")
            viewModel.addGoal()
            advanceUntilIdle()

            assertTrue(api.addGoalCalls.isEmpty())
        }

    @Test
    fun `addGoal clears the form and refreshes on success`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.onNewGoalProjectChanged(3)
            viewModel.onNewGoalHoursChanged("8")
            viewModel.addGoal()
            advanceUntilIdle()

            assertEquals(listOf(Triple("3", "8", "Weekly")), api.addGoalCalls)
            assertEquals("", viewModel.newGoalHours)
            assertNull(viewModel.newGoalProjectId)
        }

    @Test
    fun `addGoal failure surfaces the error without clearing the form`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            advanceUntilIdle()
            api.addGoalErrorMessage = "bad request"

            viewModel.onNewGoalHoursChanged("8")
            viewModel.addGoal()
            advanceUntilIdle()

            assertEquals("bad request", viewModel.uiState.errorMessage)
            assertEquals("8", viewModel.newGoalHours)
        }

    @Test
    fun `confirmDelete deletes the pending goal and clears it`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val goal = GoalDto(id = 42, hours = 5, interval = "Monthly")
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.requestDelete(goal)
            assertEquals(goal, viewModel.pendingDelete)
            viewModel.confirmDelete()
            advanceUntilIdle()

            assertEquals(listOf(42), api.deleteGoalCalls)
            assertNull(viewModel.pendingDelete)
        }

    @Test
    fun `cancelDelete clears the pending goal without deleting`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val goal = GoalDto(id = 42, hours = 5, interval = "Monthly")
            val viewModel = viewModel()
            advanceUntilIdle()

            viewModel.requestDelete(goal)
            viewModel.cancelDelete()

            assertNull(viewModel.pendingDelete)
            assertTrue(api.deleteGoalCalls.isEmpty())
        }
}
