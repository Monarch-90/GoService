package com.avetiso.feature_schedule.calendar.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state = _state.asStateFlow()

    fun handleEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.DateSelected -> {
                // Игнорируем клик, если дата уже выбрана
                if (_state.value.selectedDate == event.date) return

                _state.update { it.copy(selectedDate = event.date) }
            }
            is CalendarEvent.MonthScrolled -> {
                _state.update { it.copy(visibleMonth = event.month) }
            }
        }
    }
}