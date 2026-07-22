@file:OptIn(ExperimentalCoroutinesApi::class)

package org.mtier.timetracker.ui.timer

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mtier.timetracker.MainDispatcherRule
import org.mtier.timetracker.data.api.dto.RunningIntervalDto
import org.mtier.timetracker.data.api.dto.StartTimerRequest
import org.mtier.timetracker.data.api.dto.WorkIntervalsResponse
import org.mtier.timetracker.data.repository.ProjectsRepository
import org.mtier.timetracker.data.repository.TagsRepository
import org.mtier.timetracker.data.repository.TimerRepository
import org.mtier.timetracker.fakes.FakeProjectsCache
import org.mtier.timetracker.fakes.FakeTagsCache
import org.mtier.timetracker.fakes.FakeTimeTrackerApi
import java.time.Instant

/**
 * Regression coverage for the cross-device sync bug fixed this session:
 * TimerViewModel's init{} always starts an infinite sync-poll loop (and,
 * once a timer is running, an infinite ticker too). Two things follow:
 * - tests use runCurrent()/advanceTimeBy(), never advanceUntilIdle().
 * - every test MUST cancel viewModel.viewModelScope, in a finally block,
 *   before its runTest{} body returns. runTest's own internal wind-down
 *   drains the scheduler to idle regardless of what the test body calls,
 *   and an uncancelled infinite loop hangs that drain forever — an early
 *   assertion failure skipping a bare (non-finally) cancel() call is
 *   exactly what caused that once, and cost real debugging time to find.
 */
class TimerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeTimeTrackerApi()

    private fun viewModel(): TimerViewModel =
        TimerViewModel(TimerRepository(api), ProjectsRepository(api, FakeProjectsCache()), TagsRepository(api, FakeTagsCache()))

    @Test
    fun `the sync poller keeps re-querying work intervals on a timer, unprompted`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            try {
                runCurrent()
                assertTrue(api.getWorkIntervalsCalls.isNotEmpty())

                // Advance virtual time well past the 5s sync-poll interval with
                // no user action in between.
                advanceTimeBy(SYNC_POLL_INTERVAL_MS_FOR_TEST)
                runCurrent()

                assertTrue(api.getWorkIntervalsCalls.size >= 2)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `picking an explicit preset stops the range from sliding on the next poll`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            try {
                runCurrent()

                val fixedFrom = Instant.ofEpochSecond(1_000)
                val fixedTo = Instant.ofEpochSecond(2_000)
                viewModel.applyPreset(fixedFrom to fixedTo)
                runCurrent()

                advanceTimeBy(SYNC_POLL_INTERVAL_MS_FOR_TEST)
                runCurrent()

                assertEquals(fixedFrom, viewModel.rangeFrom)
                assertEquals(fixedTo, viewModel.rangeTo)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `refresh maps a running interval into isRunning and starts the ticker`() =
        runTest(mainDispatcherRule.testDispatcher) {
            api.workIntervalsResponse =
                WorkIntervalsResponse(running = listOf(RunningIntervalDto(id = 1, name = "task", start = Instant.now().epochSecond)))
            val viewModel = viewModel()
            try {
                runCurrent()

                assertTrue(viewModel.uiState.isRunning)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `submitting work input while idle starts a timer with the double-encoded trimmed name`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = viewModel()
            try {
                runCurrent()

                viewModel.onWorkInputChanged("  write tests  ")
                viewModel.onSubmitWorkInput()
                runCurrent()

                // "write tests" -> once: "write+tests" -> twice: the literal
                // "+" is itself re-encoded (see ApiNameEncodingTest).
                assertEquals("write%2Btests" to StartTimerRequest("", ""), api.startTimerCalls.single())
                assertEquals("", viewModel.workInput)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `submitting work input while running stops the timer instead`() =
        runTest(mainDispatcherRule.testDispatcher) {
            api.workIntervalsResponse =
                WorkIntervalsResponse(running = listOf(RunningIntervalDto(id = 1, name = "task", start = Instant.now().epochSecond)))
            val viewModel = viewModel()
            try {
                runCurrent()

                viewModel.onSubmitWorkInput()
                runCurrent()

                // "no description" double-encoded, same as ApiNameEncodingTest.
                assertEquals(listOf("no%2Bdescription"), api.stopTimerCalls)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    @Test
    fun `a failed refresh surfaces the error message and stops loading`() =
        runTest(mainDispatcherRule.testDispatcher) {
            api.workIntervalsError = RuntimeException("HTTP 500")
            val viewModel = viewModel()
            try {
                runCurrent()

                assertEquals("HTTP 500", viewModel.uiState.errorMessage)
                assertTrue(!viewModel.uiState.isLoading)
            } finally {
                viewModel.viewModelScope.cancel()
            }
        }

    private companion object {
        const val SYNC_POLL_INTERVAL_MS_FOR_TEST = 6_000L
    }
}
