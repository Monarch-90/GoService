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
import com.avetiso.feature_schedule.model.ScheduleAppointmentItem
import com.avetiso.feature_schedule.model.ScheduleBannerAdItem
import com.avetiso.feature_schedule.model.ScheduleListItem
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
        set(value) {
            savedStateHandle[ScheduleConstants.Pending.KEY_SCHEDULE_PENDING_DELETE_ID] = value
        }

    private var pendingNoteAppointmentId: Long?
        get() = savedStateHandle[ScheduleConstants.Pending.KEY_SCHEDULE_PENDING_NOTE_ID]
        set(value) {
            savedStateHandle[ScheduleConstants.Pending.KEY_SCHEDULE_PENDING_NOTE_ID] = value
        }

    private val _scheduleState = MutableStateFlow<ScheduleState>(ScheduleState.Idle)
    val scheduleState = _scheduleState.asStateFlow()

    private val selectedDate = MutableStateFlow("")

    private val appointmentsForDate: StateFlow<List<Appointment>> = selectedDate
        .flatMapLatest { date ->
            if (date.isBlank()) {
                flowOf(emptyList())
            } else {
                appointmentDao.getAppointmentsForDate(date)
            }
        }
        .map { entityList ->
            entityList.map { entity ->
                appointmentMapper.mapToUiModel(entity)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val listItems: StateFlow<List<ScheduleListItem>> = appointmentsForDate
        .map { appointments ->
            buildList {
                add(ScheduleBannerAdItem)

                appointments.forEach { appointment ->
                    add(ScheduleAppointmentItem(appointment))
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = listOf(ScheduleBannerAdItem)
        )

    fun loadAppointmentsForDate(date: String) {
        selectedDate.value = date
    }

    fun updateAppointmentStatus(
        appointmentId: Long,
        newStatus: AppointmentStatus,
        newDate: String? = null
    ) {
        viewModelScope.launch {
            when (
                val result = actionHandler.updateAppointmentStatus(
                    appointmentId,
                    newStatus,
                    newDate
                )
            ) {
                is AppointmentActionResult.Error -> {
                    _scheduleState.value = ScheduleState.Error(result.messageResId)
                }

                is AppointmentActionResult.RescheduleSuccess -> {
                    _scheduleState.value = ScheduleState.Success
                }

                is AppointmentActionResult.Success -> Unit
            }
        }
    }

    fun resetScheduleState() {
        _scheduleState.value = ScheduleState.Idle
    }

    fun onDeleteIconClicked(id: Long) {
        pendingDeleteId = id
    }

    fun onDeleteConfirmed() {
        val id = pendingDeleteId ?: return

        viewModelScope.launch {
            actionHandler.confirmDelete(id)
            pendingDeleteId = null
        }
    }

    fun onEditNoteClicked(appointmentId: Long) {
        pendingNoteAppointmentId = appointmentId
    }

    fun onNoteDialogResult(newText: String) {
        val id = pendingNoteAppointmentId ?: return

        viewModelScope.launch {
            actionHandler.updateNote(id, newText)
            pendingNoteAppointmentId = null
        }
    }
}
