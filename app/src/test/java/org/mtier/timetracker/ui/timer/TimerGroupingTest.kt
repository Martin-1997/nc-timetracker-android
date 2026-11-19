package org.mtier.timetracker.ui.timer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mtier.timetracker.data.api.dto.DayGroupDto
import org.mtier.timetracker.data.api.dto.WorkIntervalItemDto
import java.time.ZoneOffset

class TimerGroupingTest {
    private fun entry(
        id: Int,
        name: String,
        start: Long,
        duration: Int?,
    ) = WorkIntervalItemDto(id = id, name = name, start = start, duration = duration, running = if (duration == null) 1 else 0)

    @Test
    fun `groups are ordered by their newest entry and entries newest first`() {
        val days =
            buildDayGroups(
                mapOf(
                    "2026-11-18" to
                        mapOf(
                            "Mail" to DayGroupDto(listOf(entry(1, "Mail", 100, 10), entry(3, "Mail", 300, 30))),
                            "Code" to DayGroupDto(listOf(entry(2, "Code", 200, 20))),
                        ),
                ),
            )

        val groups = days.single().groups
        assertEquals(listOf("Mail", "Code"), groups.map { it.name })
        assertEquals(listOf(3, 1), groups.first().entries.map { it.id })
        assertEquals(40L, groups.first().stoppedSeconds)
        assertEquals(60L, days.single().stoppedSeconds)
    }

    @Test
    fun `a running entry marks its group and day as running`() {
        val day =
            buildDayGroups(
                mapOf("d" to mapOf("Code" to DayGroupDto(listOf(entry(1, "Code", 100, 10), entry(2, "Code", 200, null))))),
            ).single()

        assertTrue(day.isRunning)
        assertTrue(day.groups.single().isRunning)
        assertEquals(10L, day.stoppedSeconds)
    }

    @Test
    fun `formatStartEnd shows start and end, or only start while running`() {
        assertEquals("09:00 - 10:30", formatStartEnd(entry(1, "a", 32_400, 5_400), ZoneOffset.UTC))
        assertEquals("09:00 -", formatStartEnd(entry(1, "a", 32_400, null), ZoneOffset.UTC))
    }
}
