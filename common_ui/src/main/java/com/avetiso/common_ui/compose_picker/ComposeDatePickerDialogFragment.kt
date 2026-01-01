package com.avetiso.common_ui.compose_picker

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toDrawable
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.setFragmentResult
import com.avetiso.common_ui.CommonConstants
import com.avetiso.core.AppConstants

class ComposeDatePickerDialogFragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setContent {
            // Используем вашу существующую кастомную тему
            ThemeComposePicker {
                DatePickerDialogContent(
                    title = requireArguments().getString(ARG_TITLE, ""),
                    initialDateMillis = requireArguments().getLong(ARG_INITIAL_DATE, System.currentTimeMillis()),
                    onConfirm = { selectedMillis ->
                        val requestKey = requireArguments().getString(ARG_REQUEST_KEY) ?: CommonConstants.Result.DATE_PICKER
                        val extraId = requireArguments().getLong(ARG_EXTRA_ID, AppConstants.ID_NONE)

                        // Возвращаем результат (Дата + ID записи)
                        setFragmentResult(requestKey, bundleOf(
                            AppConstants.Result.RESULT_DATE to selectedMillis,
                            AppConstants.Result.DATE_RESULT_EXTRA_ID to extraId
                        ))
                        dismiss()
                    },
                    onDismiss = { dismiss() }
                )
            }
        }
    }

    // Делаем фон системного окна прозрачным, чтобы были видны наши скругления
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_INITIAL_DATE = "arg_initial_date"
        private const val ARG_REQUEST_KEY = "arg_request_key"
        private const val ARG_EXTRA_ID = "arg_extra_id"

        fun newInstance(
            requestKey: String,
            title: String,
            initialDateMillis: Long? = null,
            extraId: Long = AppConstants.ID_NONE // Передаем ID записи внутрь диалога
        ): ComposeDatePickerDialogFragment {
            return ComposeDatePickerDialogFragment().apply {
                arguments = bundleOf(
                    ARG_REQUEST_KEY to requestKey,
                    ARG_TITLE to title,
                    ARG_INITIAL_DATE to (initialDateMillis ?: System.currentTimeMillis()),
                    ARG_EXTRA_ID to extraId
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialogContent(
    title: String,
    initialDateMillis: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)

    // ЗДЕСЬ ВСЯ КАСТОМИЗАЦИЯ ЦВЕТОВ
    val dialogColors = DatePickerDefaults.colors(
        // Основной фон и шапка
        containerColor = MaterialTheme.colorScheme.surface, // Фон всего диалога (#FF0F0F0F)
        titleContentColor = MaterialTheme.colorScheme.primary, // Цвет заголовка ("Выберите дату")
        headlineContentColor = MaterialTheme.colorScheme.primary, // Цвет подзаголовка ("Выбранная дата")
        weekdayContentColor = MaterialTheme.colorScheme.onSecondary, // Цвет дней недели (Пн, Вт...)
        dayContentColor = MaterialTheme.colorScheme.onSurface, // Цвет цифр

        // Полоса переключения месяца/года
        subheadContentColor = MaterialTheme.colorScheme.primary, // Цвет текста "Сентябрь 2025"
        navigationContentColor = MaterialTheme.colorScheme.primary, // Цвет стрелок < >

        // Окно выбора года
        yearContentColor = MaterialTheme.colorScheme.onSurface, // цвет цифр года
        selectedYearContainerColor = MaterialTheme.colorScheme.secondary, // Фон выделенного года (#435E6F)
        selectedYearContentColor = MaterialTheme.colorScheme.onSurfaceVariant, // цвет цифры выделенного года

        // Цвет выделенной даты
        selectedDayContainerColor = MaterialTheme.colorScheme.secondary, // (#435E6F)
        selectedDayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,

        // Цвет кольца вокруг сегодняшней даты
        todayDateBorderColor = MaterialTheme.colorScheme.onTertiary.copy(alpha = 0.5f), // Сделаем полупрозрачным
        todayContentColor = MaterialTheme.colorScheme.onSurface,
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                // Передаем выбранную дату только если она не null
                datePickerState.selectedDateMillis?.let { onConfirm(it) }
            }) { Text("ОК", color = MaterialTheme.colorScheme.onPrimaryContainer) } // 👈 Цвет кнопки
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = MaterialTheme.colorScheme.onPrimary) } // 👈 Цвет кнопки
        },
        colors = dialogColors // Применяем все наши цвета
    ) {
        DatePicker(
            state = datePickerState,
            title = { Text(text = title, modifier = Modifier.padding(start = 24.dp, top = 24.dp)) },
            // КАСТОМИЗАЦИЯ ПОДЗАГОЛОВКА "ВЫБРАННАЯ ДАТА"
            headline = {
                // ПРИМЕНЯЕМ СТИЛЬ ЧЕРЕЗ ProvideTextStyle
                ProvideTextStyle(
                    value = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Normal
                    )
                ) {
                    // Внутри этого блока у всего текста будет наш стиль
                    DatePickerDefaults.DatePickerHeadline(
                        selectedDateMillis = datePickerState.selectedDateMillis,
                        displayMode = datePickerState.displayMode,
                        dateFormatter = remember { DatePickerDefaults.dateFormatter() },
                        modifier = Modifier.padding(start = 24.dp, bottom = 12.dp)
                    )
                }
            },
            colors = dialogColors // Применяем цвета и к самому пикеру
        )
    }
}