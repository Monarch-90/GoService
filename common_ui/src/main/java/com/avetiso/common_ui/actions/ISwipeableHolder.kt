package com.avetiso.common_ui.actions

import android.view.View

/**
 * Необязательный интерфейс для ViewHolder'ов,
 * которые должны поддерживать жест "swipe-to-reveal".
 */
interface ISwipeableHolder {
    /**
     * Контейнер с основным контентом, который будет сдвигаться при свайпе.
     */
    val contentContainer: View
}