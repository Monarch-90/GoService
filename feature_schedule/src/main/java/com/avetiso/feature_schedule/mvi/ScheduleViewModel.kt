package com.avetiso.feature_schedule.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.entity.AppointmentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao
) : ViewModel() {

    // Этот Flow будет хранить дату, для которой нужно загрузить записи
    private val _selectedDate = MutableStateFlow("")

    // flatMapLatest автоматически перезапросит данные из БД при изменении даты
    val appointmentsForDate: StateFlow<List<AppointmentEntity>> = _selectedDate
        .flatMapLatest { date ->
            if (date.isNotBlank()) {
                appointmentDao.getAppointmentsForDate(date)
            } else {
                flowOf(emptyList()) // Если дата не выбрана, возвращаем пустой список
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadAppointmentsForDate(date: String) {
        _selectedDate.value = date
    }
}