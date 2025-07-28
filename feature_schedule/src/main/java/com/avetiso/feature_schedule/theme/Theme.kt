// В файле ui/theme/Theme.kt
package com.avetiso.feature_schedule.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 1. Палитра для ТЁМНОЙ темы
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC), // purple_200
    secondary = Color(0xFF03DAC5), // teal_200
    surface = Color(0xFF1E1E1E), // цвет фона диалога
    onSurface = Color(0xFFFFFFFF) // цвет текста на фоне диалога
)

// 2. Палитра для СВЕТЛОЙ темы
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE), // purple_500
    secondary = Color(0xFF03DAC5), // teal_200
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF000000)
)

// 3. Наш главный композбл темы
@Composable
fun MyPickerAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}