package org.mtier.timetracker.ui.common

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.mtier.timetracker.R

/** Maps a report group1/group2/timegroup API value ("project", "userUid",
 *  "day", ""...) to its display label — shared by Reports and Timelines,
 *  whose group-by/interval controls use the exact same value set. */
@Composable
fun reportKeyLabel(key: String): String =
    when (key) {
        "project" -> stringResource(R.string.common_project)
        "userUid" -> stringResource(R.string.common_user)
        "client" -> stringResource(R.string.common_client)
        "name" -> stringResource(R.string.reports_time_entry)
        "day" -> stringResource(R.string.common_daily)
        "week" -> stringResource(R.string.common_weekly)
        "month" -> stringResource(R.string.common_monthly)
        "year" -> stringResource(R.string.common_yearly)
        else -> stringResource(R.string.common_none)
    }

/** A dropdown over report group-by/interval keys, labeled via [reportKeyLabel]
 *  and formatted into [labelRes] (e.g. "Group by: %1$s"). */
@Composable
fun KeyDropdown(
    selected: String,
    options: List<String>,
    @StringRes labelRes: Int,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(labelRes, reportKeyLabel(selected)))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(reportKeyLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
