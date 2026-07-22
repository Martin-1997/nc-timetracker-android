package org.mtier.timetracker.data.repository

import org.mtier.timetracker.data.api.TimeTrackerApi
import org.mtier.timetracker.data.api.dto.ReportItemDto
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportsRepository
    @Inject
    constructor(
        private val api: TimeTrackerApi,
    ) {
        @Suppress("LongParameterList")
        suspend fun getReport(
            from: Instant,
            to: Instant,
            group1: String,
            group2: String,
            timegroup: String,
            filterProjectIds: List<Int>,
            filterClientIds: List<Int>,
        ): List<ReportItemDto> =
            api
                .getReport(
                    name = "",
                    from = from.epochSecond,
                    to = to.epochSecond,
                    group1 = group1,
                    group2 = group2,
                    timegroup = timegroup,
                    filterProjectId = filterProjectIds.joinToString(","),
                    filterClientId = filterClientIds.joinToString(","),
                ).items
    }
