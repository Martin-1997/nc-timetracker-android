package org.mtier.timetracker.ui.timer

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mtier.timetracker.data.repository.ProjectsRepository
import org.mtier.timetracker.data.repository.TagsRepository
import org.mtier.timetracker.data.repository.TimerRepository
import org.mtier.timetracker.fakes.FakeProjectsCache
import org.mtier.timetracker.fakes.FakeTagsCache
import org.mtier.timetracker.fakes.FakeTimeTrackerApi
import org.mtier.timetracker.ui.theme.TimeTrackerTheme

/**
 * Exercises the critical daily-use path (PLAN.md §8 milestone 3) end to end
 * through the real Compose UI, TimerViewModel, and repository layer — only
 * [FakeTimeTrackerApi] stands in for the network, so this is as close to
 * the production code path as an instrumented test gets without a live
 * server.
 */
@RunWith(AndroidJUnit4::class)
class TimerScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    private fun timerViewModel(api: FakeTimeTrackerApi): TimerViewModel =
        TimerViewModel(
            timerRepository = TimerRepository(api),
            projectsRepository = ProjectsRepository(api, FakeProjectsCache()),
            tagsRepository = TagsRepository(api, FakeTagsCache()),
        )

    @Test
    fun startingAndStoppingATimerShowsItInTheCompletedList() {
        val api = FakeTimeTrackerApi()
        val viewModel = timerViewModel(api)

        composeRule.setContent {
            TimeTrackerTheme {
                TimerScreen(viewModel = viewModel)
            }
        }

        composeRule.onNodeWithText("What have you done?").performTextInput("write instrumented tests")
        composeRule.onNodeWithText("Start").performClick()
        composeRule.waitForIdle()

        // Now running: the button relabels to Stop and the entry isn't in
        // the completed list yet.
        composeRule.onNodeWithText("Stop").assertExists()
        composeRule.onNodeWithText("write instrumented tests").assertDoesNotExist()

        composeRule.onNodeWithText("Stop").performClick()
        composeRule.waitForIdle()

        // Stopped: back to Start, and the entry now shows in the list.
        composeRule.onNodeWithText("Start").assertExists()
        composeRule.onNodeWithText("write instrumented tests").assertExists()
    }
}
