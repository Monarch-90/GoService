package com.avetiso.feature_statistics.mvi

import androidx.annotation.StringRes

/**
 * Сайд-эффекты экрана статистики (одноразовые события, которые не должны сохраняться при повороте экрана).
 * Строго без вложенности (nested classes).
 */
sealed interface StatisticsEvent

// --- Навигация ---
object NavigateToCompletedAppointments : StatisticsEvent
object NavigateToInventory : StatisticsEvent
object NavigateToFrequentClients : StatisticsEvent

// --- Системные действия ---
/**
 * Копирует текст в буфер обмена и показывает Toast об успешном копировании.
 * @param textToCopy Сгенерированный текст (свободные окна, прайс или список покупок).
 * @param successMessageResId Строковый ресурс для Toast (чтобы избежать голых строк в коде).
 */
data class CopyToClipboard(
    val textToCopy: String,
    @StringRes val successMessageResId: Int
) : StatisticsEvent

// --- Уведомления ---
/**
 * Показ обычного уведомления.
 */
data class ShowToast(
    @StringRes val messageResId: Int
) : StatisticsEvent