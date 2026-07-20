package org.mtier.timetracker.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/** Multi-select as toggleable chips — used for locked-project allowed
 *  tags/users and Reports/Timelines' project/client filters. [T] is the
 *  id type (Int for tags/projects/clients, String for user uids). */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun <T> ToggleChipGroup(
    items: List<Pair<T, String>>,
    selectedIds: Set<T>,
    onToggle: (T) -> Unit,
) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (id, label) ->
            FilterChip(
                selected = id in selectedIds,
                onClick = { onToggle(id) },
                label = { Text(label) },
            )
        }
    }
}
