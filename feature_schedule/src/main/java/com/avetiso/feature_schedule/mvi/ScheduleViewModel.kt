package com.avetiso.feature_schedule.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.action.AppointmentActionHandler
import com.avetiso.core.action.AppointmentActionResult
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.entity.ui.Appointment
import com.avetiso.core.mapper.AppointmentMapper
import com.avetiso.core.models.AppointmentStatus
import com.avetiso.feature_schedule.ScheduleConstants
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
    private val actionHandler: AppointmentActionHandler,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var pendingDeleteId: Long?
        get() = savedStateHandle[ScheduleConstants.Pending.KEY_SCHEDULE_PENDING_DELETE_ID]
        set(value) { savedStateHandle[ScheduleConstants.Pending.KEY_SCHEDULE_PENDING_DELETE_ID] = value }

    private var pendingNoteAppointmentId: Long?
        get() = savedStateHandle[ScheduleConstants.Pending.KEY_SCHEDULE_PENDING_NOTE_ID]
        set(value) { savedStateHandle[ScheduleConstants.Pending.KEY_SCHEDULE_PENDING_NOTE_ID] = value }

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
        pendingDeleteId = id
    }

    // 2. Подтвердили удаление
    fun onDeleteConfirmed() {
        val id = pendingDeleteId ?: return // Достаем ID
        viewModelScope.launch {
            actionHandler.confirmDelete(id) // Передаем ID в Stateless UseCase
            pendingDeleteId = null          // Сбрасываем после успеха
        }
    }

    // 1. Фрагмент вызывает это, когда открывает диалог
    fun onEditNoteClicked(appointmentId: Long) {
        pendingNoteAppointmentId = appointmentId
    }

    // 2. Фрагмент вызывает это, когда диалог вернул результат
    fun onNoteDialogResult(newText: String) {
        val id = pendingNoteAppointmentId ?: return // Достаем ID (выживет даже после сворачивания)
        viewModelScope.launch {
            actionHandler.updateNote(id, newText) // Передаем ID и текст
            pendingNoteAppointmentId = null       // Сбрасываем
        }
    }
}