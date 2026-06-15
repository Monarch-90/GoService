package com.avetiso.common_ui.compose_picker.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.avetiso.common_ui.R
import com.avetiso.common_ui.compose_picker.getCustomDatePickerColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialogContent(
    title: String,
    onConfirm: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    // Стейт инкапсулирован, корректно переживает поворот экрана внутри Compose
    val dateRangePickerState = rememberDateRangePickerState()
    val dialogColors = getCustomDatePickerColors()

    Surface(
        modifier = Modifier.fillMaxSize(), // Занимает всю высоту 85%, которую мы задали во Фрагменте!
        shape = RoundedCornerShape(24.dp), // Твои фирменные скругления
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 2. Сам календарь. weight(1f) заставит его растянуться на всё свободное пространство!
            DateRangePicker(
                state = dateRangePickerState,
                modifier = Modifier.weight(1f),
                title = {
                    Text(
                        text = title,
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp)
                    )
                },
                headline = {
                    CustomDateRangeHeadline(
                        selectedStartDateMillis = dateRangePickerState.selectedStartDateMillis,
                        selectedEndDateMillis = dateRangePickerState.selectedEndDateMillis
                    )
                },
                colors = dialogColors,
                showModeToggle = false
            )

            // 3. Рисуем кнопки сами внизу
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp, end = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                TextButton(onClick = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        onConfirm(start, end)
                    }
                }) {
                    Text(
                        text = stringResource(R.string.ok),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Изолированная приватная функция для форматирования заголовка диапазона дат.
 * Полностью решает проблему с отсутствующим DateRangePickerHeadline в SDK.
 */
@Composable
private fun CustomDateRangeHeadline(
    selectedStartDateMillis: Long?,
    selectedEndDateMillis: Long?,
    modifier: Modifier = Modifier
) {
    // Берем строки строго из ресурсов (защита от голых стрингов)
    val startDateText = formatHeadlineDate(selectedStartDateMillis) ?: stringResource(R.string.date_start)
    val endDateText = formatHeadlineDate(selectedEndDateMillis) ?: stringResource(R.string.date_end)

    Text(
        text = "$startDateText — $endDateText",
        style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(start = 24.dp, bottom = 12.dp)
    )
}

/**
 * Изолированный форматтер для UI-слоя.
 */
private fun formatHeadlineDate(millis: Long?): String? {
    if (millis == null) return null
    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}