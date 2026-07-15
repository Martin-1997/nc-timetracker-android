package org.mtier.timetracker.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.mtier.timetracker.R

/**
 * Shared list+add+edit+delete UI for the simple "id + name" entities
 * (Tags, Clients). Projects needs its own screen — it has color/client/
 * locked/archived fields the other two don't.
 */
@Composable
fun <T> NamedEntityCrudScreen(
    modifier: Modifier,
    items: List<T>,
    getId: (T) -> Int,
    getName: (T) -> String,
    isLoading: Boolean,
    errorMessage: String?,
    newName: String,
    onNewNameChanged: (String) -> Unit,
    newNameLabel: String,
    onAdd: () -> Unit,
    addLabel: String,
    onEditRequested: (T) -> Unit,
    onDeleteRequested: (T) -> Unit,
    editingName: String?,
    onEditingNameChanged: (String) -> Unit,
    onSaveEdit: () -> Unit,
    onCancelEdit: () -> Unit,
    editDialogTitle: String,
    pendingDeleteName: String?,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
) {
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newName,
                onValueChange = onNewNameChanged,
                label = { Text(newNameLabel) },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            Button(onClick = onAdd, modifier = Modifier.padding(start = 8.dp)) {
                Text(addLabel)
            }
        }

        errorMessage?.let {
            Text(text = it, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
        }

        if (isLoading && items.isEmpty()) {
            CircularProgressIndicator(modifier = Modifier.padding(top = 32.dp))
        } else {
            LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                items(items, key = { getId(it) }) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(text = getName(item), modifier = Modifier.weight(1f))
                        IconButton(onClick = { onEditRequested(item) }) {
                            Icon(Icons.Filled.Edit, contentDescription = null)
                        }
                        IconButton(onClick = { onDeleteRequested(item) }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }

    if (editingName != null) {
        AlertDialog(
            onDismissRequest = onCancelEdit,
            title = { Text(editDialogTitle) },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = onEditingNameChanged,
                    singleLine = true,
                )
            },
            confirmButton = { Button(onClick = onSaveEdit) { Text(stringResource(R.string.timer_save)) } },
            dismissButton = {
                Button(onClick = onCancelEdit) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }

    if (pendingDeleteName != null) {
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title = { Text(stringResource(R.string.timer_delete_confirm_title)) },
            text = { Text(pendingDeleteName) },
            confirmButton = {
                Button(onClick = onConfirmDelete) { Text(stringResource(R.string.common_confirm)) }
            },
            dismissButton = {
                Button(onClick = onCancelDelete) { Text(stringResource(R.string.common_cancel)) }
            },
        )
    }
}
