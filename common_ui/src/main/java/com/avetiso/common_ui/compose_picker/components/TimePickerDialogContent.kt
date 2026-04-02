package com.avetiso.common_ui.compose_picker.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.avetiso.common_ui.R
import com.chargemap.compose.numberpicker.NumberPicker

@Composable
fun TimePickerDialogContent(
    title: String,
    initialHour: Int,
    initialMinute: Int,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    // Состояние инкапсулировано внутри UI компонента
    var selectedHour by rememberSaveable { mutableIntStateOf(initialHour) }
    var selectedMinute by rememberSaveable { mutableIntStateOf(initialMinute) }

    val minuteDisplayValues = (0..55 step 5).map { String.format("%02d", it) }
    val minuteIndex = selectedMinute / 5

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.primary,
        textContentColor = MaterialTheme.colorScheme.primary,
        text = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                NumberPicker(
                    value = selectedHour,
                    onValueChange = { selectedHour = it },
                    range = 0..23,
                    label = { "$it ч" },
                    dividersColor = MaterialTheme.colorScheme.secondary,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary)
                )
                NumberPicker(
                    value = minuteIndex,
                    onValueChange = { newIndex -> selectedMinute = newIndex * 5 },
                    range = 0 until minuteDisplayValues.size,
                    label = { minuteDisplayValues[it] + " мин" },
                    dividersColor = MaterialTheme.colorScheme.secondary,
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.primary)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedHour, selectedMinute) }) {
                Text(stringResource(R.string.ОК), color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.Отмена), color = MaterialTheme.colorScheme.onPrimary)
            }
        }
    )
}