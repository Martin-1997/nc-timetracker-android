package org.mtier.timetracker.ui.common

import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val DATE_LABEL_FORMATTER = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val DATE_TIME_LABEL_FORMATTER =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withZone(ZoneId.systemDefault())

private fun combineDateAndTime(
    dateUtcMillis: Long,
    hour: Int,
    minute: Int,
): Instant = pickerMillisToDate(dateUtcMillis).atTime(hour, minute).atZone(ZoneId.systemDefault()).toInstant()

/**
 * Date-only picker button for one end of a date range (Timer, Dashboard,
 * Reports, Timelines). [value] is a range bound; see [DateBound] for what
 * the start and end bounds of a picked day are.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerButton(
    value: Instant,
    onValueChanged: (Instant) -> Unit,
    bound: DateBound = DateBound.START,
) {
    var showDialog by remember { mutableStateOf(false) }
    val date = boundToDate(value, bound)

    OutlinedButton(onClick = { showDialog = true }) {
        Text(DATE_LABEL_FORMATTER.format(date))
    }

    if (showDialog) {
        val state = rememberDatePickerState(initialSelectedDateMillis = dateToPickerMillis(date))
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                Button(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onValueChanged(dateToBound(pickerMillisToDate(millis), bound))
                    }
                    showDialog = false
                }) { Text("OK") }
            },
        ) {
            DatePicker(state = state)
        }
    }
}

/** Date+time picker button, used for manual entry / edit-time forms. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerButton(
    value: Instant,
    onValueChanged: (Instant) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickedDateMillis by remember { mutableStateOf<Long?>(null) }

    OutlinedButton(onClick = { showDatePicker = true }) {
        Text(DATE_TIME_LABEL_FORMATTER.format(value))
    }

    if (showDatePicker) {
        val dateState =
            rememberDatePickerState(
                initialSelectedDateMillis = dateToPickerMillis(value.atZone(ZoneId.systemDefault()).toLocalDate()),
            )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    pickedDateMillis = dateState.selectedDateMillis
                    showDatePicker = false
                    showTimePicker = true
                }) { Text("OK") }
            },
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTimePicker) {
        val localValue = value.atZone(ZoneId.systemDefault())
        val timeState =
            rememberTimePickerState(
                initialHour = localValue.hour,
                initialMinute = localValue.minute,
                is24Hour = true,
            )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                Button(onClick = {
                    val dateMillis = pickedDateMillis
                    if (dateMillis != null) {
                        onValueChanged(combineDateAndTime(dateMillis, timeState.hour, timeState.minute))
                    }
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
        )
    }
}
