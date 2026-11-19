package org.mtier.timetracker.data.repository

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mtier.timetracker.fakes.FakeTimeTrackerApi
import java.time.Instant
import java.util.TimeZone

/** mtierltd/timetracker#151: each time is sent with the offset of its own date. */
class TimerRepositoryDstTest {
    private val api = FakeTimeTrackerApi()
    private val repository = TimerRepository(api)
    private lateinit var originalTimeZone: TimeZone

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Berlin"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
    }

    @Test
    fun `tzOffsetMinutesAt uses the offset of the given date`() {
        assertEquals(-120, tzOffsetMinutesAt(Instant.parse("2026-07-01T08:00:00Z")))
        assertEquals(-60, tzOffsetMinutesAt(Instant.parse("2026-11-18T08:00:00Z")))
    }

    @Test
    fun `addManualEntry sends the summer offset for a July entry`() =
        runTest {
            repository.addManualEntry(
                "Work",
                "",
                Instant.parse("2026-07-01T08:00:00Z"),
                Instant.parse("2026-07-01T09:00:00Z"),
            )

            val body = api.addWorkIntervalCalls.single().second
            assertEquals("01/07/26 10:00", body.start)
            assertEquals(-120, body.tzoffset)
            assertEquals(-120, body.tzoffsetEnd)
        }

    @Test
    fun `updateTime sends separate offsets across the night of a clock change`() =
        runTest {
            // 2026-10-25 03:00 CEST -> 02:00 CET in Berlin.
            repository.updateTime(
                1,
                Instant.parse("2026-10-24T22:00:00Z"),
                Instant.parse("2026-10-25T06:00:00Z"),
            )

            val body = api.updateTimeCalls.single().second
            assertEquals(-120, body.tzoffset)
            assertEquals(-60, body.tzoffsetEnd)
        }
}
