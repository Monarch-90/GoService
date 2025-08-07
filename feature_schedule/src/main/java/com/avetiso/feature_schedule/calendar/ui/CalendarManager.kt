package com.avetiso.feature_schedule.calendar.ui

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import com.avetiso.feature_schedule.calendar.mvi.CalendarEvent
import com.avetiso.feature_schedule.calendar.mvi.CalendarState
import com.avetiso.feature_schedule.calendar.mvi.CalendarViewModel
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.WeekFields
import java.util.Locale

class CalendarManager(
    private val calendarView: CalendarView,
    private val viewModel: CalendarViewModel,
    private val context: Context,
) {
    private val today = LocalDate.now()
    private var previousSelectedDate: LocalDate? = viewModel.state.value.selectedDate

    fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(currentMonth)

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer.create(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                bindDay(container, data)
            }
        }

        calendarView.monthScrollListener = { month ->
            viewModel.handleEvent(CalendarEvent.MonthScrolled(month.yearMonth))
        }
    }

    fun observeState(newState: CalendarState) {
        val newSelectedDate = newState.selectedDate

        // Если дата действительно изменилась...
        if (previousSelectedDate != newSelectedDate) {
            // ...обновляем и старую, и новую дату в UI календаря
            previousSelectedDate?.let { calendarView.notifyDateChanged(it) }
            calendarView.notifyDateChanged(newSelectedDate)

            // ...и запоминаем новую дату как предыдущую для следующего раза
            previousSelectedDate = newSelectedDate
        }

    }

    private fun bindDay(container: DayViewContainer, data: CalendarDay) {
        val textView = container.binding.dayText
        val selectedDate = viewModel.state.value.selectedDate

        textView.text = data.date.dayOfMonth.toString()
        container.binding.root.setOnClickListener {
            if (data.position == DayPosition.MonthDate) {
                viewModel.handleEvent(CalendarEvent.DateSelected(data.date))
            }
        }

        // Сброс стилей
        textView.background = null

        textView.setTextColor(
            ContextCompat.getColor(
                context,
                com.avetiso.core.R.color.custom_black_white
            )
        )

        if (data.position == DayPosition.MonthDate) {
            textView.visibility = View.VISIBLE
            when (data.date) {
                selectedDate -> {
                    textView.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    textView.setBackgroundResource(com.avetiso.core.R.color.custom_main)
                }

                today -> {
                    textView.setBackgroundResource(com.avetiso.core.R.color.surface_color)
                    textView.setTextColor(
                        ContextCompat.getColor(
                            context,
                            com.avetiso.core.R.color.custom_black_white
                        )
                    )
                }
            }
        } else {
            textView.visibility = View.INVISIBLE
        }
    }
}