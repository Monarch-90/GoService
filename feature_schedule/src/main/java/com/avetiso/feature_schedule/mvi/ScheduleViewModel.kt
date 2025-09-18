package com.avetiso.feature_schedule.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.entity.AppointmentEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow("")

    val appointmentsForDate: StateFlow<List<AppointmentEntity>> = _selectedDate
        .flatMapLatest { date ->
            if (date.isBlank()) {
                flowOf(emptyList())
            } else {
                appointmentDao.getAppointmentsForDate(date)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadAppointmentsForDate(date: String) {
        _selectedDate.value = date
    }

    fun deleteAppointment(appointmentId: Long) {
        viewModelScope.launch {
            val appointmentToDelete = appointmentDao.getAppointmentById(appointmentId)
            appointmentToDelete?.let {
                appointmentDao.deleteAppointment(it)
            }
        }
    }
}