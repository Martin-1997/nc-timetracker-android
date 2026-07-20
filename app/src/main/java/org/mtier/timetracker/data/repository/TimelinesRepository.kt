package org.mtier.timetracker.data.repository

import org.mtier.timetracker.data.api.TimeTrackerApi
import org.mtier.timetracker.data.api.dto.EditTimelineStatusRequest
import org.mtier.timetracker.data.api.dto.EmailTimelineRequest
import org.mtier.timetracker.data.api.dto.TimelineDto
import org.mtier.timetracker.data.api.throwOnError
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimelinesRepository
    @Inject
    constructor(
        private val api: TimeTrackerApi,
    ) {
        suspend fun getTimelines(): List<TimelineDto> = api.getTimelines().timelines

        suspend fun getTimelinesAdmin(): List<TimelineDto> = api.getTimelinesAdmin().timelines

        /** See [TimeTrackerApi.postTimeline]'s kdoc: exports the current
         *  report as a saved timeline snapshot server-side. */
        @Suppress("LongParameterList")
        suspend fun exportTimeline(
            from: Instant,
            to: Instant,
            group1: String,
            group2: String,
            timegroup: String,
            filterProjectIds: List<Int>,
            filterClientIds: List<Int>,
        ) {
            api
                .postTimeline(
                    name = "",
                    from = from.epochSecond,
                    to = to.epochSecond,
                    group1 = group1,
                    group2 = group2,
                    timegroup = timegroup,
                    filterProjectId = filterProjectIds.joinToString(","),
                    filterClientId = filterClientIds.joinToString(","),
                ).throwOnError()
        }

        suspend fun editStatus(
            id: Int,
            status: String,
        ) {
            api.editTimeline(id, EditTimelineStatusRequest(status)).throwOnError()
        }

        suspend fun deleteTimeline(id: Int) {
            api.deleteTimeline(id).throwOnError()
        }

        suspend fun emailTimeline(
            id: Int,
            email: String,
            subject: String,
            content: String,
        ) {
            api.emailTimeline(id, EmailTimelineRequest(email, subject, content)).throwOnError()
        }

        suspend fun downloadTimelineCsv(id: Int): ByteArray = api.downloadTimeline(id).bytes()
    }
