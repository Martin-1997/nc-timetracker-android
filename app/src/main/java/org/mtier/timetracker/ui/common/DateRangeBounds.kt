package org.mtier.timetracker.ui.common

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Which end of a date range a date picker sets.
 *
 * A range covers whole days in the phone's time zone: [START] is midnight at
 * the beginning of the picked day, [END] is midnight at the beginning of the
 * day after it, so entries of the picked end day are included — the same
 * bounds the quick range presets use (see `resolvePresetRange`).
 */
enum class DateBound { START, END }

/**
 * Material3's `DatePicker` selects a calendar date as milliseconds at
 * midnight UTC of that date, regardless of the phone's time zone.
 */
fun pickerMillisToDate(millis: Long): LocalDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()

fun dateToPickerMillis(date: LocalDate): Long = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

/** The range bound for a picked [date], in [zone]. */
fun dateToBound(
    date: LocalDate,
    bound: DateBound,
    zone: ZoneId = ZoneId.systemDefault(),
): Instant =
    when (bound) {
        DateBound.START -> date.atStartOfDay(zone).toInstant()
        DateBound.END -> date.plusDays(1).atStartOfDay(zone).toInstant()
    }

/**
 * The calendar date a range bound stands for, in [zone] — the date to show on
 * the button and to preselect in the picker. An [DateBound.END] bound is
 * exclusive (midnight after the last day), so it belongs to the day before;
 * a bound that isn't exactly midnight, like the default "now", belongs to its
 * own day either way.
 */
fun boundToDate(
    value: Instant,
    bound: DateBound,
    zone: ZoneId = ZoneId.systemDefault(),
): LocalDate =
    when (bound) {
        DateBound.START -> value.atZone(zone).toLocalDate()
        DateBound.END -> value.minusNanos(1).atZone(zone).toLocalDate()
    }
