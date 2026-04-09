package com.avetiso.feature_schedule.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.action.AppointmentActionHandler
import com.avetiso.core.action.AppointmentActionResult
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.entity.ui.Appointment
import com.avetiso.core.mapper.AppointmentMapper
import com.avetiso.core.models.AppointmentStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val appointmentMapper: AppointmentMapper,
    val actionHandler: AppointmentActionHandler,
) : ViewModel() {

    private val _scheduleState = MutableStateFlow<ScheduleState>(ScheduleState.Idle)
    val scheduleState = _scheduleState.asStateFlow()
    private val _selectedDate = MutableStateFlow("")
    val appointmentsForDate: StateFlow<List<Appointment>> = _selectedDate
        .flatMapLatest { date ->
            if (date.isBlank()) {
                flowOf(emptyList())
            } else {
                appointmentDao.getAppointmentsForDate(date)
            }
        }
        .map { entityList -> entityList.map { appointmentMapper.mapToUiModel(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadAppointmentsForDate(date: String) {
        _selectedDate.value = date
    }

    fun updateAppointmentStatus(appointmentId: Long, newStatus: AppointmentStatus, newDate: String? = null) {
        viewModelScope.launch {
            when (val result = actionHandler.updateAppointmentStatus(appointmentId, newStatus, newDate)) {
                is AppointmentActionResult.Error -> {
                    _scheduleState.value = ScheduleState.Error(result.messageResId)
                }

                is AppointmentActionResult.RescheduleSuccess -> {
                    _scheduleState.value = ScheduleState.Success
                }

                is AppointmentActionResult.Success -> {
                    // Успешно обновили статус, UI-стейт менять не нужно
                }
            }
        }
    }

    fun resetScheduleState() {
        _scheduleState.value = ScheduleState.Idle
    }

    // 1. Фрагмент передает ID (так как у него есть только Appointment c ID)
    fun onDeleteIconClicked(id: Long) {
        actionHandler.appointmentPendingDeleteId = id
    }

    // 2. Подтвердили удаление
    fun onDeleteConfirmed() {
        viewModelScope.launch { actionHandler.confirmDelete() }
    }

    // 1. Фрагмент вызывает это, когда открывает диалог
    fun onEditNoteClicked(appointmentId: Long) {
        actionHandler.pendingAppointmentId = appointmentId
    }

    // 2. Фрагмент вызывает это, когда диалог вернул результат
    fun onNoteDialogResult(newText: String) {
        viewModelScope.launch { actionHandler.updateNote(newText) }
    }
}