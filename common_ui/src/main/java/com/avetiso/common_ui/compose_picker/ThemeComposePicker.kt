package com.avetiso.common_ui.compose_picker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.DatePickerColors
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import com.avetiso.core.R

@Composable
fun ThemeComposePicker(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    // Палитры теперь определяются здесь, внутри @Composable функции

    // 1. Палитра для ТЁМНОЙ темы из ресурсов
    val darkColorScheme = darkColorScheme(
        primary = colorResource(R.color.white), // цвет кнопок, текста и цифр
        secondary = colorResource(R.color.custom_main), // цвет полос
        surface = colorResource(R.color.custom_dialog_background), // цвет фона
        onSecondary = colorResource(android.R.color.darker_gray), // цвет дней недели
        onSurface = colorResource(R.color.grey_and_dark), // цвет числа даты
        onTertiary = colorResource(R.color.active_item_bg_color), // цвет кольца вокруг сегодняшней даты
        onPrimary = colorResource(R.color.grey), // цвет кнопки Отмена (дата пикер)
        onSurfaceVariant = colorResource(R.color.grey_and_white), // цвет числа выделенной даты
        onPrimaryContainer = colorResource(R.color.dark_grey), // Цвет кнопки Ок (дата пикер)
    )

    // 2. Палитра для СВЕТЛОЙ темы из ресурсов
    val lightColorScheme = lightColorScheme(
        primary = colorResource(R.color.black), // цвет кнопок, текста и цифр
        secondary = colorResource(R.color.custom_main), // цвет полос
        surface = colorResource(R.color.custom_dialog_background), // цвет фона
        onSecondary = colorResource(android.R.color.darker_gray), // цвет наименования дней недели
        onSurface = colorResource(R.color.grey_and_dark), // цвет числа даты
        onTertiary = colorResource(R.color.active_item_bg_color), // цвет кольца вокруг сегодняшней даты
        onPrimary = colorResource(R.color.grey), // цвет кнопки Отмена (дата пикер)
        onSurfaceVariant = colorResource(R.color.grey_and_white), // цвет числа выделенной даты
        onPrimaryContainer = colorResource(R.color.dark_grey), // Цвет кнопки Ок (дата пикер)
    )

    val colorScheme = when {
        darkTheme -> darkColorScheme
        else -> lightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}

/**
 * Универсальные цвета для всех календарей (Single и Range).
 * Строгое соблюдение DRY: настраиваем цвета в одном месте.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun getCustomDatePickerColors(): DatePickerColors {
    return DatePickerDefaults.colors(
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

        // Дополнительные цвета для диапазона (DateRangePicker)
        dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
        dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onSurface
    )
}