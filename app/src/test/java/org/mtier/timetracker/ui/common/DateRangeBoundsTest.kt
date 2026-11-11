package org.mtier.timetracker.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class DateRangeBoundsTest {
    private val lima = ZoneId.of("America/Lima") // UTC-5
    private val tokyo = ZoneId.of("Asia/Tokyo") // UTC+9
    private val berlin = ZoneId.of("Europe/Berlin")
    private val nov10 = LocalDate.of(2026, 11, 10)

    @Test
    fun `picker millis are midnight UTC of the picked date`() {
        val millis = Instant.parse("2026-11-10T00:00:00Z").toEpochMilli()
        assertEquals(nov10, pickerMillisToDate(millis))
        assertEquals(millis, dateToPickerMillis(nov10))
    }

    @Test
    fun `start bound is local midnight at the beginning of the picked day`() {
        assertEquals(Instant.parse("2026-11-10T05:00:00Z"), dateToBound(nov10, DateBound.START, lima))
        assertEquals(Instant.parse("2026-11-09T15:00:00Z"), dateToBound(nov10, DateBound.START, tokyo))
    }

    @Test
    fun `end bound is local midnight after the picked day, so the whole day is included`() {
        assertEquals(Instant.parse("2026-11-11T05:00:00Z"), dateToBound(nov10, DateBound.END, lima))
        assertEquals(Instant.parse("2026-11-10T15:00:00Z"), dateToBound(nov10, DateBound.END, tokyo))

        // an entry started late on the picked end day lies inside the range
        val lateEntry = Instant.parse("2026-11-11T04:30:00Z") // 23:30 on 10 Nov in Lima
        assertTrue(lateEntry.isBefore(dateToBound(nov10, DateBound.END, lima)))
    }

    @Test
    fun `end bound on the day daylight saving time ends is 25 hours after the start`() {
        val day = LocalDate.of(2026, 10, 25)
        val hours = (dateToBound(day, DateBound.END, berlin).epochSecond - dateToBound(day, DateBound.START, berlin).epochSecond) / 3600
        assertEquals(25L, hours)
    }

    @Test
    fun `buttons show the picked date, east and west of UTC`() {
        for (zone in listOf(lima, tokyo, berlin)) {
            assertEquals(nov10, boundToDate(dateToBound(nov10, DateBound.START, zone), DateBound.START, zone))
            assertEquals(nov10, boundToDate(dateToBound(nov10, DateBound.END, zone), DateBound.END, zone))
        }
    }

    @Test
    fun `a bound that is not midnight belongs to its own local day`() {
        val evening = Instant.parse("2026-11-11T02:00:00Z") // 21:00 on 10 Nov in Lima, 11:00 on 11 Nov in Tokyo
        assertEquals(nov10, boundToDate(evening, DateBound.END, lima))
        assertEquals(nov10, boundToDate(evening, DateBound.START, lima))
        assertEquals(LocalDate.of(2026, 11, 11), boundToDate(evening, DateBound.END, tokyo))
    }

    @Test
    fun `the end of a quick range preset shows its last day`() {
        // resolvePresetRange ends "today" at midnight after today, like DateBound.END
        val presetEnd = nov10.plusDays(1).atStartOfDay(lima).toInstant()
        assertEquals(nov10, boundToDate(presetEnd, DateBound.END, lima))
    }
}
