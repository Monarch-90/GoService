package com.avetiso.feature_appointments.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.action.AppointmentActionHandler
import com.avetiso.core.action.AppointmentActionResult
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.entity.ui.Appointment
import com.avetiso.core.mapper.AppointmentMapper
import com.avetiso.feature_appointments.AppointmentsConstants
import com.avetiso.feature_appointments.model.AppointmentContentItem
import com.avetiso.feature_appointments.model.AppointmentDateHeaderItem
import com.avetiso.feature_appointments.model.AppointmentBannerAdItem
import com.avetiso.feature_appointments.model.AppointmentsListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val appointmentMapper: AppointmentMapper,
    private val actionHandler: AppointmentActionHandler,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(AppointmentsState())
    val state = _state.asStateFlow()

    private val _events = Channel<AppointmentsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val searchQuery = MutableStateFlow("")
    private val mappedAppointments = MutableStateFlow<List<Pair<String, Appointment>>>(emptyList())

    private val headerFormatter = DateTimeFormatter.ofPattern(
        AppConstants.Format.DATE_FORMAT_HEADER,
        Locale.getDefault()
    )

    private var pendingDeleteId: Long?
        get() = savedStateHandle[AppointmentsConstants.Pending.KEY_PENDING_DELETE_ID]
        set(value) {
            savedStateHandle[AppointmentsConstants.Pending.KEY_PENDING_DELETE_ID] = value
        }

    private var pendingNoteAppointmentId: Long?
        get() = savedStateHandle[AppointmentsConstants.Pending.KEY_PENDING_NOTE_ID]
        set(value) {
            savedStateHandle[AppointmentsConstants.Pending.KEY_PENDING_NOTE_ID] = value
        }

    init {
        observeAppointments()
        observeListItems()
    }

    fun processIntent(intent: AppointmentsIntent) {
        when (intent) {
            is DeleteAppointmentClickedIntent -> {
                pendingDeleteId = intent.appointmentId
                viewModelScope.launch {
                    _events.send(ShowAppointmentDeleteDialogEvent)
                }
            }

            is ConfirmAppointmentDeleteIntent -> {
                val id = pendingDeleteId ?: return
                viewModelScope.launch {
                    actionHandler.confirmDelete(id)
                    pendingDeleteId = null
                }
            }

            is EditAppointmentIntent -> {
                viewModelScope.launch {
                    _events.send(NavigateToEditAppointmentEvent(intent.appointmentId))
                }
            }

            is OpenAppointmentNoteIntent -> {
                pendingNoteAppointmentId = intent.appointmentId
                viewModelScope.launch {
                    _events.send(
                        ShowAppointmentNoteDialogEvent(
                            currentNote = intent.currentNote
                        )
                    )
                }
            }

            is SaveAppointmentNoteIntent -> {
                val id = pendingNoteAppointmentId ?: return
                viewModelScope.launch {
                    actionHandler.updateNote(id, intent.newNote)
                    pendingNoteAppointmentId = null
                }
            }

            is ChangeAppointmentStatusIntent -> {
                viewModelScope.launch {
                    val result = actionHandler.updateAppointmentStatus(
                        appointmentId = intent.appointmentId,
                        newStatus = intent.status,
                        newDate = intent.newDate
                    )

                    if (result is AppointmentActionResult.Error) {
                        _events.send(ShowAppointmentsToastEvent(result.messageResId))
                    }
                }
            }

            is UpdateAppointmentsSearchQueryIntent -> {
                searchQuery.value = intent.query
            }
        }
    }

    private fun observeAppointments() {
        appointmentDao.getAllAppointments()
            .map { entityList ->
                entityList.map { entity ->
                    entity.date to appointmentMapper.mapToUiModel(entity)
                }
            }
            .onEach { mappedList ->
                mappedAppointments.value = mappedList
            }
            .launchIn(viewModelScope)
    }

    private fun observeListItems() {
        combine(
            mappedAppointments,
            searchQuery
        ) { mappedList, query ->
            val filteredAppointments = filterAppointments(
                mappedList = mappedList,
                query = query
            )

            buildListItems(
                filteredAppointments = filteredAppointments
            )
        }
            .onEach { items ->
                _state.update { state ->
                    state.copy(
                        isLoading = false,
                        items = items
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    private fun filterAppointments(
        mappedList: List<Pair<String, Appointment>>,
        query: String
    ): List<Pair<String, Appointment>> {
        val lowerQuery = query.trim().lowercase()

        if (lowerQuery.isBlank()) {
            return mappedList
        }

        return mappedList.filter { (_, appointment) ->
            appointment.clientName.lowercase().contains(lowerQuery) ||
                    appointment.serviceNames.lowercase().contains(lowerQuery) ||
                    appointment.date.lowercase().contains(lowerQuery) ||
                    appointment.time.lowercase().contains(lowerQuery) ||
                    appointment.price.lowercase().contains(lowerQuery) ||
                    appointment.note.lowercase().contains(lowerQuery) ||
                    appointment.status.name.lowercase().contains(lowerQuery)
        }
    }

    private fun buildListItems(
        filteredAppointments: List<Pair<String, Appointment>>
    ): List<AppointmentsListItem> {
        val items = mutableListOf<AppointmentsListItem>()

        items.add(AppointmentBannerAdItem)

        filteredAppointments
            .groupBy { pair -> pair.first }
            .forEach { (rawDate, appointmentsForDate) ->
                items.add(
                    AppointmentDateHeaderItem(
                        date = formatHeaderDate(rawDate)
                    )
                )

                appointmentsForDate.forEach { (_, appointment) ->
                    items.add(
                        AppointmentContentItem(
                            appointment = appointment
                        )
                    )
                }
            }

        return items
    }

    private fun formatHeaderDate(rawDate: String): String {
        if (rawDate.isBlank()) {
            return ""
        }

        return try {
            LocalDate.parse(rawDate).format(headerFormatter)
        } catch (exception: Exception) {
            rawDate
        }
    }

}
