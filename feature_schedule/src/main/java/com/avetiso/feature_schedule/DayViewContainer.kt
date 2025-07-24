package com.avetiso.feature_schedule

import android.view.View
import android.widget.TextView
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.ViewContainer

// Добавляем в конструктор функцию-обработчик клика
class DayViewContainer(
    view: View,
    private val onDayClick: (CalendarDay) -> Unit,
) : ViewContainer(view) {

    val textView: TextView = view.findViewById(R.id.day_text)
    lateinit var day: CalendarDay // "lateinit" здесь безопасен, т.к. он всегда устанавливается в bind

    init {
        view.setOnClickListener {
            // Вызываем обработчик только для дней текущего месяца
            if (day.position == DayPosition.MonthDate) {
                onDayClick(day)
            }
        }
    }
}