package com.avetiso.feature_schedule.calendar.ui

import android.view.View
import com.avetiso.feature_schedule.databinding.CalendarDayLayoutBinding
import com.kizitonwose.calendar.view.ViewContainer

/**
 * Контейнер для ячейки дня, использующий View Binding.
 * Конструктор приватный, чтобы создание шло через удобный метод create.
 */
class DayViewContainer private constructor(
    // Храним объект биндинга, а не просто View
    val binding: CalendarDayLayoutBinding,
) : ViewContainer(binding.root) {

    companion object {
        /**
         * Фабричный метод для создания экземпляра DayViewContainer из View.
         * Это позволяет инкапсулировать логику создания биндинга.
         */
        fun create(view: View): DayViewContainer {
            // Создаем биндинг из готового View, которое надула библиотека
            val binding = CalendarDayLayoutBinding.bind(view)
            return DayViewContainer(binding)
        }
    }
}