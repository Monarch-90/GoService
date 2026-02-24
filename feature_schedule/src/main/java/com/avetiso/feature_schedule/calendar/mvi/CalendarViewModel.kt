package com.avetiso.feature_schedule.calendar.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.data.dao.AppointmentDao
import com.kizitonwose.calendar.core.yearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao,
) : ViewModel() {

    private val _state = MutableStateFlow(CalendarState())
    val state = _state.asStateFlow()

    init {
        // Следим за изменениями видимого месяца
        _state
            .map { it.visibleMonth } // Берем только видимый месяц
            .distinctUntilChanged()  // Реагируем, только если месяц сменился
            .flatMapLatest { month -> // Для каждого нового месяца делаем новый запрос в БД
                val yearMonthPattern = month.format(DateTimeFormatter.ofPattern(AppConstants.Format.DATE_FORMAT_YEAR_MONTH))
                appointmentDao.getEventDatesForMonth(yearMonthPattern)
            }
            .onEach { dateStrings -> // Когда из БД приходит список дат-строк
                // Преобразуем строки 'YYYY-MM-DD' в Set<LocalDate>
                val localDates = dateStrings.map { LocalDate.parse(it) }.toSet()
                // Обновляем состояние
                _state.update { it.copy(eventDates = localDates) }
            }
            .launchIn(viewModelScope)
    }

    fun handleEvent(event: CalendarEvent) {
        when (event) {
            is CalendarEvent.DateSelected -> {
                android.util.Log.d("ScheduleDebug", "CalendarEvent.DateSelected: event_date=${event.date}, current_state_date=${_state.value.selectedDate}")

                // Игнорируем клик, если дата уже выбрана
                if (_state.value.selectedDate == event.date) return

                _state.update { it.copy(selectedDate = event.date) }
            }

            is CalendarEvent.MonthScrolled -> {
                val currentSelectedDate = _state.value.selectedDate

                // Проверяем, есть ли выделенная дата И отличается ли ее месяц от нового видимого месяца
                if (currentSelectedDate != null && currentSelectedDate.yearMonth != event.month) {
                    // Если да - сбрасываем выделение (selectedDate = null)
                    _state.update { it.copy(selectedDate = null, visibleMonth = event.month) }
                } else {
                    // Если нет (например, даты не было или месяц тот же) - просто обновляем месяц
                    _state.update { it.copy(visibleMonth = event.month) }
                }
            }
        }
    }
}