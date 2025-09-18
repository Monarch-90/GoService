package com.avetiso.feature_schedule.add_appointment.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.data.dao.ServiceDao
import com.avetiso.core.data.dao.TimeSlotDao
import com.avetiso.core.entity.AppointmentEntity
import com.avetiso.core.entity.TimeSlotEntity
import com.avetiso.core.model.ServiceSnapshot
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
    private val clientDao: ClientDao,
    private val serviceDao: ServiceDao,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(AddAppointmentState())
    val state = _state.asStateFlow()

    private var appointmentToEditId: Long = -1L

    // канал для одноразовых событий навигации
    private val _navigationChannel = Channel<NavigationEvent>()
    val navigationEvents = _navigationChannel.receiveAsFlow()

    init {
        appointmentToEditId = savedStateHandle.get<Long>("appointmentId") ?: -1L
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
                            // ✅ ГЛАВНОЕ ИСПРАВЛЕНИЕ:
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
                _state.update {
                    it.copy(
                        // Логика переключения: если кликнули по уже выбранному, снимаем выбор
                        selectedClient = if (it.selectedClient == event.client) null else event.client,
                        isNextButtonEnabled = it.selectedClient != event.client // Кнопка активна, если мы выбрали нового клиента
                    )
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
            val gson = Gson() // Понадобится для сравнения услуг

            // --- 1. Собираем данные для НОВОЙ записи ---
            val client = currentState.selectedClient ?: return@launch
            val services = currentState.selectedServices
            val timeSlot = currentState.selectedTimeSlots.firstOrNull() ?: return@launch

            val selectedDate: String = if (appointmentToEditId != -1L) {
                // В режиме редактирования БЕРЕМ ДАТУ ИЗ СУЩЕСТВУЮЩЕЙ ЗАПИСИ
                appointmentDao.getAppointmentById(appointmentToEditId)?.date ?: return@launch
            } else {
                // В режиме создания берем дату из аргументов навигации
                savedStateHandle["selectedDate"] ?: return@launch
            }

            // --- 2. Получаем ВСЕ существующие записи на эту дату для проверки ---
            val existingAppointments = appointmentDao.getAppointmentsForDateSync(selectedDate)

            // --- 3. Создаем "снимок" услуг для НОВОЙ записи ---
            val newServiceSnapshots = services.map { service ->
                ServiceSnapshot(
                    id = service.id, name = service.name, categoryName = service.categoryName,
                    isPriceFrom = service.isPriceFrom, price = service.price,
                    currency = service.currency, durationMinutes = service.durationMinutes
                )
            }.toSet() // Превращаем в Set для сравнения без учета порядка

            // --- 4. Запускаем цикл проверки на дубликаты ---
            for (existingAppointment in existingAppointments) {
                // Пропускаем проверку с самой собой в режиме редактирования
                if (appointmentToEditId != -1L && existingAppointment.id == appointmentToEditId) {
                    continue
                }

                // Сравниваем слот времени
                val isTimeSlotSame = existingAppointment.startTimeMinutes == timeSlot.startTimeMinutes

                // Сравниваем клиента по всем полям
                val isClientSame = existingAppointment.clientName == client.name &&
                        existingAppointment.clientPhoneNumber == client.phoneNumber &&
                        existingAppointment.clientInstagram == client.instagram

                // Сравниваем набор услуг
                val existingServicesJson = existingAppointment.servicesJson
                val existingServiceSnapshots = gson.fromJson(existingServicesJson, Array<ServiceSnapshot>::class.java).toSet()
                val areServicesSame = existingServiceSnapshots == newServiceSnapshots

                // Если ВСЕ совпало - это дубликат
                if (isTimeSlotSame && isClientSame && areServicesSame) {
                    _navigationChannel.send(NavigationEvent.ShowToast("Такая запись уже существует на эту дату"))
                    return@launch // Прерываем сохранение
                }
            }

            // --- 5. Если дубликатов не найдено - СОХРАНЯЕМ ЗАПИСЬ ---
            val totalDuration = services.sumOf { it.durationMinutes }
            val servicesJson = gson.toJson(newServiceSnapshots) // Используем уже созданный JSON

            val appointmentToSave = AppointmentEntity(
                id = if (appointmentToEditId != -1L) appointmentToEditId else 0,
                date = selectedDate,
                startTimeMinutes = timeSlot.startTimeMinutes,
                totalDurationMinutes = totalDuration,
                clientName = client.name,
                clientPhoneNumber = client.phoneNumber, // Сохраняем доп. поля
                clientInstagram = client.instagram,   // Сохраняем доп. поля
                servicesJson = servicesJson
            )

            if (appointmentToEditId != -1L) {
                appointmentDao.updateAppointment(appointmentToSave)
            } else {
                appointmentDao.insertAppointment(appointmentToSave)
            }

            _navigationChannel.send(NavigationEvent.NavigateToSchedule)
        }
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