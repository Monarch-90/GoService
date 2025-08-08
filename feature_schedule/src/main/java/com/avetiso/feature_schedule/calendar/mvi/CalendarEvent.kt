package com.avetiso.feature_schedule.calendar.mvi

import java.time.LocalDate
import java.time.YearMonth

sealed interface CalendarEvent {
    data class DateSelected(val date: LocalDate) : CalendarEvent
    data class MonthScrolled(val month: YearMonth) : CalendarEvent
}