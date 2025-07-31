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
    var day: CalendarDay? = null

    init {
        view.setOnClickListener {
            // Вызываем обработчик только для дней текущего месяца
            day?.let { currentDay ->
                if (currentDay.position == DayPosition.MonthDate) {
                    onDayClick(currentDay)
                }
            }
        }
    }
}