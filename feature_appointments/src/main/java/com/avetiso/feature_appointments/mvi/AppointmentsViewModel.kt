package com.avetiso.feature_appointments.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.action.AppointmentActionHandler
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.mapper.AppointmentMapper
import com.avetiso.feature_appointments.model.AppointmentsListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val appointmentMapper: AppointmentMapper,
    val actionHandler: AppointmentActionHandler,
) : ViewModel() {

    private val _state = MutableStateFlow(AppointmentsState())
    val state = _state.asStateFlow()

    private val _events = Channel<AppointmentsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    private val _mappedAppointments = MutableStateFlow<List<Pair<String, com.avetiso.core.entity.ui.Appointment>>>(emptyList())

    // Форматтер для заголовка (например, "12 марта 2026")
    private val headerFormatter = DateTimeFormatter.ofPattern(AppConstants.Format.DATE_FORMAT_HEADER, Locale.getDefault())

    init {

        android.util.Log.d("AppTrace", "AppointmentsViewModel: INIT")
        observeAppointments()
    }

    fun processIntent(intent: AppointmentsIntent) {
        when (intent) {
            is AppointmentsIntent.OnDeleteClicked -> {
                actionHandler.appointmentPendingDeleteId = intent.appointmentId
                viewModelScope.launch {
                    _events.send(AppointmentsEvent.ShowDeleteDialog(intent.appointmentId))
                }
            }

            is AppointmentsIntent.ConfirmDelete -> {
                viewModelScope.launch { actionHandler.confirmDelete() }
            }

            is AppointmentsIntent.OnEditClicked -> {
                viewModelScope.launch {
                    _events.send(AppointmentsEvent.NavigateToEdit(intent.appointmentId))
                }
            }

            is AppointmentsIntent.OnNoteClicked -> {
                actionHandler.pendingAppointmentId = intent.appointmentId
                viewModelScope.launch {
                    _events.send(AppointmentsEvent.ShowNoteDialog(intent.appointmentId, intent.currentNote))
                }
            }

            is AppointmentsIntent.SaveNote -> {
                viewModelScope.launch { actionHandler.updateNote(intent.newNote) }
            }

            is AppointmentsIntent.ChangeStatus -> {
                viewModelScope.launch {
                    val result = actionHandler.updateAppointmentStatus(intent.appointmentId, intent.status, intent.newDate)
                    if (result is com.avetiso.core.action.AppointmentActionResult.Error) {
                        _events.send(AppointmentsEvent.ShowToast(result.messageResId))
                    }
                }
            }

            is AppointmentsIntent.UpdateSearchQuery -> _searchQuery.value = intent.query
        }
    }

    private fun observeAppointments() {
        // 1. Поток: Слушаем БД, мапим тяжелые данные ТОЛЬКО при изменении базы и кэшируем в памяти
        appointmentDao.getAllAppointments()
            .map { entityList ->
                entityList.map { entity ->
                    // Сохраняем пару: (Сырая дата из БД -> Готовая UI модель)
                    entity.date to appointmentMapper.mapToUiModel(entity)
                }
            }
            .onEach { mappedList ->
                _mappedAppointments.value = mappedList
            }
            .launchIn(viewModelScope)

        // 2. Поток: Молниеносный поиск по кэшированным UI-моделям прямо в оперативной памяти
        kotlinx.coroutines.flow.combine(_mappedAppointments, _searchQuery) { mappedList, query ->
            val lowerQuery = query.trim().lowercase()

            val filteredList = if (lowerQuery.isBlank()) {
                mappedList
            } else {
                mappedList.filter { (_, appt) ->
                    appt.clientName.lowercase().contains(lowerQuery) ||
                            appt.serviceNames.lowercase().contains(lowerQuery) ||
                            appt.date.lowercase().contains(lowerQuery) ||
                            appt.time.lowercase().contains(lowerQuery) ||
                            appt.price.lowercase().contains(lowerQuery) ||
                            appt.note.lowercase().contains(lowerQuery) ||
                            appt.status.name.lowercase().contains(lowerQuery) // Fallback для статуса
                }
            }

            val listItems = mutableListOf<AppointmentsListItem>()
            val grouped = filteredList.groupBy { it.first }

            for ((rawDate, pairs) in grouped) {
                val headerDateText = try {
                    if (rawDate.isBlank()) "" else {
                        val localDate = LocalDate.parse(rawDate)
                        localDate.format(headerFormatter)
                    }
                } catch (e: Exception) {
                    rawDate
                }

                listItems.add(AppointmentsListItem.DateHeader(headerDateText))

                for ((_, appt) in pairs) {
                    listItems.add(AppointmentsListItem.AppointmentItem(appt))
                }
            }
            listItems
        }
            .onEach { items ->
                _state.update { it.copy(isLoading = false, items = items) }
            }
            .launchIn(viewModelScope)
    }
}