package com.avetiso.common_ui.compose_picker

import androidx.compose.foundation.isSystemInDarkTheme
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
    )

    // 2. Палитра для СВЕТЛОЙ темы из ресурсов
    val lightColorScheme = lightColorScheme(
        primary = colorResource(R.color.black), // цвет кнопок, текста и цифр
        secondary = colorResource(R.color.custom_main), // цвет полос
        surface = colorResource(R.color.custom_dialog_background), // цвет фона
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