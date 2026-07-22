package org.mtier.timetracker.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.mtier.timetracker.R
import java.time.Instant

/** Quick-range dropdown + two date pickers + a refresh button — used by
 *  Timer, Reports, Dashboard, and Timelines' date range controls. */
@Composable
fun DateRangeRow(
    from: Instant,
    to: Instant,
    onFromChanged: (Instant) -> Unit,
    onToChanged: (Instant) -> Unit,
    onPreset: (Pair<Instant, Instant>) -> Unit,
    onRefresh: () -> Unit,
) {
    var presetMenuExpanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 12.dp)) {
        Column {
            OutlinedButton(onClick = { presetMenuExpanded = true }) {
                Text(stringResource(R.string.timer_quick_range))
            }
            DropdownMenu(expanded = presetMenuExpanded, onDismissRequest = { presetMenuExpanded = false }) {
                DATE_RANGE_PRESETS.forEach { preset ->
                    DropdownMenuItem(
                        text = { Text(preset.label) },
                        onClick = {
                            presetMenuExpanded = false
                            resolvePresetRange(preset.key)?.let(onPreset)
                        },
                    )
                }
            }
        }
        DatePickerButton(value = from, onValueChanged = onFromChanged)
        Text("–", modifier = Modifier.padding(horizontal = 4.dp))
        DatePickerButton(value = to, onValueChanged = onToChanged)
        Button(onClick = onRefresh, modifier = Modifier.padding(start = 4.dp)) {
            Text(stringResource(R.string.common_retry))
        }
    }
}
