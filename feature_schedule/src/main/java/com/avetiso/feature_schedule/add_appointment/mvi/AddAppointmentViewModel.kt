package com.avetiso.feature_schedule.add_appointment.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.entity.AppointmentEntity
import com.avetiso.core.entity.ClientEntity
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.core.entity.TimeSlotEntity
import com.avetiso.core.models.AppointmentStatus
import com.avetiso.core.models.ServiceSnapshot
import com.avetiso.feature_schedule.ScheduleConstants
import com.avetiso.core.mapper.AppointmentPriceMapper
import com.avetiso.feature_schedule.add_appointment.ui.ADD_APPOINTMENT_PAGE_COUNT
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddAppointmentViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val savedStateHandle: SavedStateHandle,
    private val uiMapper: AppointmentPriceMapper,
) : ViewModel() {

    private val _state = MutableStateFlow(AddAppointmentState())
    val state = _state.asStateFlow()

    private var appointmentToEditId: Long = AppConstants.ID_NONE

    // канал для одноразовых событий навигации
    private val _navigationChannel = Channel<NavigationEvent>()
    val navigationEvents = _navigationChannel.receiveAsFlow()

    init {
        appointmentToEditId =
            savedStateHandle.get<Long>(ScheduleConstants.Args.APPOINTMENT_ID) ?: AppConstants.ID_NONE
    }

    fun handleEvent(event: AddAppointmentEvent) {
        when (event) {
            is AddAppointmentEvent.NextButtonClicked -> {
                val currentStep = _state.value.currentStep
                if (currentStep < ADD_APPOINTMENT_PAGE_COUNT - 1) {
                    val nextStep = currentStep + 1
                    _state.update {
                        it.copy(
                            currentStep = nextStep,
                            isNextButtonEnabled = isStepComplete(nextStep, it) // Проверяем сразу
                        )
                    }
                } else {
                    saveAppointment()
                }
            }

            is AddAppointmentEvent.BackPressed -> {
                val currentStep = _state.value.currentStep
                if (currentStep > 0) {
                    val previousStep = currentStep - 1
                    _state.update {
                        it.copy(
                            currentStep = previousStep,
                            // При возврате на шаг назад, мы ПЕРЕСЧИТЫВАЕМ состояние кнопки
                            // на основе уже имеющихся данных в state.
                            isNextButtonEnabled = isStepComplete(previousStep, it)
                        )
                    }
                }
            }

            // Управляем выбором услуг здесь
            is AddAppointmentEvent.ServiceSelected -> {
                _state.update { currentState ->
                    val newSelection = currentState.selectedServices.toMutableSet()
                    if (event.isSelected) {
                        newSelection.add(event.service)
                    } else {
                        newSelection.remove(event.service)
                    }
                    // Обновляем и список услуг, и состояние кнопки
                    currentState.copy(
                        selectedServices = newSelection,
                        isNextButtonEnabled = newSelection.isNotEmpty()
                    )
                }
            }

            is AddAppointmentEvent.TimeSlotClicked -> {
                _state.update { currentState ->
                    val newSelection = currentState.selectedTimeSlots.toMutableSet()
                    // Если слот уже есть в наборе, удаляем (снятие выделения).
                    // Иначе - добавляем (выделение).
                    if (newSelection.contains(event.timeSlot)) {
                        newSelection.remove(event.timeSlot)
                    } else {
                        newSelection.add(event.timeSlot)
                    }
                    val newState = currentState.copy(selectedTimeSlots = newSelection)
                    newState.copy(
                        isNextButtonEnabled = isStepComplete(
                            newState.currentStep,
                            newState
                        )
                    )
                }
            }

            is AddAppointmentEvent.ClearTimeSlotSelection -> {
                _state.update { currentState ->
                    val newState = currentState.copy(selectedTimeSlots = emptySet())
                    newState.copy(
                        isNextButtonEnabled = isStepComplete(
                            newState.currentStep,
                            newState
                        )
                    )
                }
            }

            is AddAppointmentEvent.ClientSelected -> {
                _state.update { currentState ->
                    // 1. Сначала обновляем состояние с выбранным клиентом.
                    val updatedState = currentState.copy(selectedClient = event.client)

                    // 2. Затем, на основе этого НОВОГО состояния, вычисляем,
                    //    должна ли кнопка быть активна, используя нашу общую функцию.
                    updatedState.copy(isNextButtonEnabled = isStepComplete(updatedState.currentStep, updatedState))
                }
            }

            is AddAppointmentEvent.ClearClientSelection -> {
                _state.update {
                    it.copy(
                        selectedClient = null,
                        isNextButtonEnabled = false
                    )
                }
            }

            is AddAppointmentEvent.NavigateToAddService -> {
                viewModelScope.launch {
                    _navigationChannel.send(NavigationEvent.NavigateToAddService)
                }
            }

            is AddAppointmentEvent.ClearSelection -> {
                _state.update { currentState ->
                    currentState.copy(
                        selectedServices = emptySet(), // Очищаем список выделенных услуг
                        isNextButtonEnabled = false      // Выключаем кнопку "Далее"
                    )
                }
            }
        }
    }

    private fun saveAppointment() {
        viewModelScope.launch {
            val currentState = _state.value
            val client = currentState.selectedClient ?: return@launch
            val services = currentState.selectedServices
            val timeSlots = currentState.selectedTimeSlots

            // 1. Проверяем на дубликат с помощью новой чистой функции
            if (isDuplicate(client, services, timeSlots)) {
                val errorMessage = uiMapper.getDuplicateErrorString()
                _navigationChannel.send(NavigationEvent.ShowToast(errorMessage))
                return@launch
            }

            // 2. Если все в порядке, создаем и сохраняем сущность
            // 1. Определяем статус для сохранения
            val statusToSave: AppointmentStatus = if (appointmentToEditId != AppConstants.ID_NONE) {
                // Теперь dao возвращает Enum, нам не нужно гадать со строками
                appointmentDao.getAppointmentById(appointmentToEditId)?.status
                    ?: AppointmentStatus.ACTIVE
            } else {
                // Для новой записи
                AppointmentStatus.ACTIVE
            }

            // 2. Создаем сущность с правильным статусом
            val appointmentToSave = createAppointmentEntity(client, services, statusToSave, timeSlots)

            if (appointmentToEditId != AppConstants.ID_NONE) {
                appointmentDao.updateAppointment(appointmentToSave)
            } else {
                appointmentDao.insertAppointment(appointmentToSave)
            }

            _navigationChannel.send(NavigationEvent.NavigateToSchedule)
        }
    }

    // Проверяет, существует ли уже аналогичная запись на эту дату.
    private suspend fun isDuplicate(
        client: ClientEntity,
        services: Set<ServiceEntity>,
        timeSlots: Set<TimeSlotEntity>,
    ): Boolean {
        val argDate = savedStateHandle.get<String>(ScheduleConstants.Args.SELECTED_DATE)

        // БЕЗОПАСНОЕ ПОЛУЧЕНИЕ ДАТЫ: Если дата из навигатора пустая, берем оригинал из БД
        val selectedDate = if (argDate.isNullOrBlank()) {
            appointmentDao.getAppointmentById(appointmentToEditId)?.date ?: return true
        } else {
            argDate
        }

        val existingAppointments = appointmentDao.getAppointmentsForDateSync(selectedDate)
        val newServiceSnapshots = services.map { it.toSnapshot() }.toSet()
        val newTimeSlotIds = timeSlots.map { it.id }.sorted()

        for (existing in existingAppointments) {
            if (appointmentToEditId != AppConstants.ID_NONE && existing.id == appointmentToEditId) continue

            val isTimeSlotSame = existing.timeSlotIds.sorted() == newTimeSlotIds
            val isClientSame = existing.clientName == client.name &&
                    existing.clientPhoneNumber == client.phoneNumber &&
                    existing.clientInstagram == client.instagram

            val existingServices = Gson().fromJson(existing.servicesJson, Array<ServiceSnapshot>::class.java).toSet()
            val areServicesSame = existingServices == newServiceSnapshots

            if (isTimeSlotSame && isClientSame && areServicesSame) {
                return true // Найден дубликат
            }
        }
        return false // Дубликатов нет
    }

    // Создает и возвращает готовую к сохранению сущность AppointmentEntity.
    private suspend fun createAppointmentEntity(
        client: ClientEntity,
        services: Set<ServiceEntity>,
        status: AppointmentStatus,
        timeSlots: Set<TimeSlotEntity>,
    ): AppointmentEntity {
        // Достаем оригинальную запись (чтобы не потерять дату и заметку при редактировании)
        val existingAppointment = if (appointmentToEditId != AppConstants.ID_NONE) {
            appointmentDao.getAppointmentById(appointmentToEditId)
        } else null

        val argDate = savedStateHandle.get<String>(ScheduleConstants.Args.SELECTED_DATE)
        val selectedDate = if (argDate.isNullOrBlank()) {
            existingAppointment?.date ?: ""
        } else {
            argDate
        }

        val totalDuration = services.sumOf { it.durationMinutes }
        val serviceSnapshots = services.map { it.toSnapshot() }
        val servicesJson = Gson().toJson(serviceSnapshots)

        val earliestStartTime = timeSlots.minOfOrNull { it.startTimeMinutes } ?: 0

        return AppointmentEntity(
            id = if (appointmentToEditId != AppConstants.ID_NONE) appointmentToEditId else 0,
            date = selectedDate,
            startTimeMinutes = earliestStartTime,
            timeSlotIds = timeSlots.map { it.id }.sorted(),
            totalDurationMinutes = totalDuration,
            clientName = client.name,
            clientPhoneNumber = client.phoneNumber,
            clientInstagram = client.instagram,
            discountPercent = client.discount,
            status = status,
            servicesJson = servicesJson,
            // ВАЖНО: сохраняем существующую заметку, чтобы она не стерлась при редактировании
            note = existingAppointment?.note ?: ""
        )
    }

    private fun ServiceEntity.toSnapshot(): ServiceSnapshot {
        return ServiceSnapshot(
            id = this.id,
            name = this.name,
            categoryName = this.categoryName,
            isPriceFrom = this.isPriceFrom,
            price = this.price,
            currency = this.currency,
            durationMinutes = this.durationMinutes,
        )
    }

    // Проверяет, завершен ли шаг
    private fun isStepComplete(step: Int, state: AddAppointmentState): Boolean {
        return when (step) {
            0 -> state.selectedServices.isNotEmpty()
            1 -> state.selectedTimeSlots.isNotEmpty()
            2 -> state.selectedClient != null
            else -> false
        }
    }
}