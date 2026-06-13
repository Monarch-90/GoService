package com.avetiso.common_ui.compose_picker.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avetiso.common_ui.R
import com.avetiso.common_ui.compose_picker.getCustomDatePickerColors
import com.avetiso.core.AppConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialogContent(
    title: String,
    initialDateMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Стейт инкапсулирован внутри Compose, переживает рекомпозиции
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    // Берем нашу Enterprise-тему
    val dialogColors = getCustomDatePickerColors()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Передаем выбранную дату только если она не null
                datePickerState.selectedDateMillis?.let { onConfirm(it) }
            }) {
                Text(
                    text = stringResource(R.string.ok),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        colors = dialogColors
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp)
                )
            },
            headline = {
                // ИСПОЛЬЗУЕМ НАШ КАСТОМНЫЙ, БЕЗОПАСНЫЙ КОМПОНЕНТ
                CustomDatePickerHeadline(
                    selectedDateMillis = datePickerState.selectedDateMillis
                )
            },
            colors = dialogColors,
            showModeToggle = false
        )
    }
}

/**
 * Приватная Composable-функция (не нарушает правило об отсутствии вложенных классов).
 * Изолирует логику форматирования заголовка выбранной даты.
 */
@Composable
private fun CustomDatePickerHeadline(
    selectedDateMillis: Long?,
    modifier: Modifier = Modifier
) {
    // Если дата еще не выбрана (теоретически невозможно при initialDateMillis, но требует Kotlin-безопасности)
    val dateText = if (selectedDateMillis != null) {
        val formatter = SimpleDateFormat(AppConstants.Format.FULL_DATE_FORMAT, Locale.getDefault())
        formatter.format(Date(selectedDateMillis))
    } else {
        ""
    }

    Text(
        text = dateText,
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 24.dp, bottom = 12.dp)
    )
}