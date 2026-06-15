package com.avetiso.feature_schedule.calendar.ui

import android.content.Context
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.avetiso.feature_schedule.R
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
    private var previousEventDates: Set<LocalDate> = emptySet()

    fun setupCalendar(initialMonth: YearMonth) {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val firstDayOfWeek = WeekFields.of(Locale.getDefault()).firstDayOfWeek

        calendarView.setup(startMonth, endMonth, firstDayOfWeek)
        calendarView.scrollToMonth(initialMonth)

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
        val newEventDates = newState.eventDates

        if (previousSelectedDate != newSelectedDate || previousEventDates != newEventDates) {
            // Если что-то изменилось, даем календарю простую команду
            // полностью перерисовать видимые ячейки. Это эффективно и надежно.
            calendarView.notifyCalendarChanged()

            // Обновляем наши "трекеры" для следующей проверки
            previousSelectedDate = newSelectedDate
            previousEventDates = newEventDates
        }

    }

    private fun bindDay(container: DayViewContainer, data: CalendarDay) {
        val textView = container.binding.dayText
        val selectedDate = viewModel.state.value.selectedDate

        // 1. ПОЛНЫЙ СБРОС СТИЛЕЙ для каждой ячейки перед настройкой
        // Это решает проблемы с "переезжанием" стилей при прокрутке.
        textView.text = data.date.dayOfMonth.toString()
        textView.typeface = null // Сбрасываем жирность
        textView.setTextSize(
            TypedValue.COMPLEX_UNIT_SP,
            14f
        ) // Возвращаем стандартный размер (например, 14sp)
        textView.background = null
        textView.setTextColor(
            ContextCompat.getColor(
                context,
                com.avetiso.core.R.color.custom_black_white
            )
        )
        container.binding.root.background = null

        container.binding.root.setOnClickListener {

            if (data.position == DayPosition.MonthDate) {
                viewModel.handleEvent(CalendarEvent.DateSelected(data.date))
            }
        }

        if (data.position == DayPosition.MonthDate) {
            textView.visibility = View.VISIBLE
            container.dotIndicator.isVisible = data.date in viewModel.state.value.eventDates

            when {
                // Случай 1: Дата является и сегодняшней, и выделенной
                data.date == today && data.date == selectedDate -> {
                    // Текст белый, как выделенный
                    textView.setTextColor(ContextCompat.getColor(context, com.avetiso.core.R.color.white))
                    textView.setBackgroundResource(R.drawable.calendar_today_selected_bg)
                }
                // Случай 2: Дата просто сегодняшняя (но не выделенная)
                data.date == today -> {
                    textView.setBackgroundResource(R.drawable.calendar_today_bg)
                }
                // Случай 3: Дата просто выделенная (но не сегодняшняя)
                data.date == selectedDate -> {
                    textView.setTextColor(ContextCompat.getColor(context, com.avetiso.core.R.color.white))
                    textView.setBackgroundResource(R.drawable.calendar_day_selected_bg)
                }
            }
        } else {
            textView.visibility = View.INVISIBLE
            container.dotIndicator.isVisible = false
        }
    }
}