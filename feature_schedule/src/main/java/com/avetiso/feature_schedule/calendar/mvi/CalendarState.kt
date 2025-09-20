package com.avetiso.feature_schedule.calendar.mvi

import java.time.LocalDate
import java.time.YearMonth

data class CalendarState(
    val selectedDate: LocalDate = LocalDate.now(),
    val visibleMonth: YearMonth = YearMonth.now(),
    val eventDates: Set<LocalDate> = emptySet()
)