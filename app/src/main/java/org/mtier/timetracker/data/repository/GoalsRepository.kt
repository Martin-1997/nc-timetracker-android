package org.mtier.timetracker.data.repository

import org.mtier.timetracker.data.api.TimeTrackerApi
import org.mtier.timetracker.data.api.dto.GoalDto
import org.mtier.timetracker.data.api.throwOnError
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalsRepository
    @Inject
    constructor(
        private val api: TimeTrackerApi,
    ) {
        suspend fun getGoals(): List<GoalDto> = api.getGoals().goals

        suspend fun addGoal(
            projectId: Int?,
            hours: String,
            interval: String,
        ) {
            api.addGoal(projectId?.toString() ?: "", hours, interval).throwOnError()
        }

        suspend fun deleteGoal(id: Int) {
            api.deleteGoal(id).throwOnError()
        }
    }
