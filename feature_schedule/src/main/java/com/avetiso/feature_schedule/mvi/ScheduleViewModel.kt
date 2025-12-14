package com.avetiso.feature_schedule.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.data.dao.TimeSlotDao
import com.avetiso.core.entity.AppointmentEntity
import com.avetiso.core.model.ServiceSnapshot
import com.avetiso.feature_schedule.add_appointment.data.Appointment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val timeSlotDao: TimeSlotDao,
) : ViewModel() {

    private val _scheduleState = MutableStateFlow<ScheduleState>(ScheduleState.Idle)
    val scheduleState = _scheduleState.asStateFlow()

    private val _selectedDate = MutableStateFlow("")
    private val gson = Gson()
    private var pendingAppointmentId: Long? = null

    val appointmentsForDate: StateFlow<List<Appointment>> = _selectedDate
        .flatMapLatest { date ->
            if (date.isBlank()) {
                flowOf(emptyList())
            } else {
                appointmentDao.getAppointmentsForDate(date)
            }
        }
        .map { entityList ->
            entityList.map { entity ->
                // Вся логика маппинга теперь внутри ViewModel
                mapToAppointment(entity)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private suspend fun mapToAppointment(appointmentEntity: AppointmentEntity): Appointment {
        val listType = object : TypeToken<List<ServiceSnapshot>>() {}.type
        val services: List<ServiceSnapshot> =
            gson.fromJson(appointmentEntity.servicesJson, listType) ?: emptyList()

        // 1. ПОЛУЧАЕМ СПИСОК ID СЛОТОВ ИЗ ЗАПИСИ
        val timeSlotIds = appointmentEntity.timeSlotIds

        // 2. ЗАГРУЖАЕМ ВСЕ ОБЪЕКТЫ СЛОТОВ ИЗ БД
        val timeSlots = timeSlotDao.getTimeSlotsByIds(timeSlotIds)
            .sortedBy { it.startTimeMinutes } // Сортируем по времени

        // 3. ФОРМАТИРУЕМ КАЖДЫЙ СЛОТ И ОБЪЕДИНЯЕМ В ОДНУ СТРОКУ
        val timeString = timeSlots.joinToString(separator = "\n") { slot ->
            val hours = slot.startTimeMinutes / 60
            val minutes = slot.startTimeMinutes % 60
            String.format("%02d:%02d", hours, minutes)
        }

        val serviceNamesString = services.joinToString(", ") { it.name }
        val discountPercent = appointmentEntity.discountPercent

        val rawDate = LocalDate.parse(appointmentEntity.date)
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)
        val formattedDate = rawDate.format(formatter)

        val priceString = services
            .groupBy { it.currency }
            .map { (currency, servicesInCurrency) ->
                val total = servicesInCurrency.sumOf { it.price }
                val isPriceFrom = servicesInCurrency.any { it.isPriceFrom }
                val prefix = if (isPriceFrom) "от " else ""

                val finalTotal = if (discountPercent > 0) {
                    total * (1 - discountPercent / 100.0)
                } else {
                    total
                }

                "$prefix${"%.2f".format(finalTotal)} $currency"
            }
            .joinToString("\n")

        return Appointment(
            id = appointmentEntity.id,
            time = timeString,
            serviceNames = serviceNamesString,
            clientName = appointmentEntity.clientName,
            price = priceString,
            hasDiscount = discountPercent > 0,
            status = appointmentEntity.status,
            note = appointmentEntity.note,
            date = formattedDate,
        )
    }

    fun loadAppointmentsForDate(date: String) {
        _selectedDate.value = date
    }

    fun updateAppointmentStatus(appointmentId: Long, newStatus: String, newDate: String? = null) {
        viewModelScope.launch {
            val appointmentToUpdate = appointmentDao.getAppointmentById(appointmentId) ?: return@launch

            // 🎯 1. ПРОВЕРЯЕМ, НЕ ЯВЛЯЕТСЯ ЛИ ПЕРЕНОС "ФИКТИВНЫМ" (НА ТУ ЖЕ ДАТУ)
            val isReschedulingToSameDate = newDate != null && newDate == appointmentToUpdate.date

            if (newDate != null && !isReschedulingToSameDate) {
                val existingAppointmentsOnNewDate = appointmentDao.getAppointmentsForDateSync(newDate)

                for (existing in existingAppointmentsOnNewDate) {
                    // Сравниваем слоты, клиента и услуги
                    val isTimeSlotSame = existing.timeSlotIds.sorted() == appointmentToUpdate.timeSlotIds.sorted()
                    val isClientSame = existing.clientName == appointmentToUpdate.clientName &&
                            existing.clientPhoneNumber == appointmentToUpdate.clientPhoneNumber &&
                            existing.clientInstagram == appointmentToUpdate.clientInstagram
                    val areServicesSame = existing.servicesJson == appointmentToUpdate.servicesJson

                    if (isTimeSlotSame && isClientSame && areServicesSame) {
                        // Если все совпало - это дубликат. Отправляем событие и выходим.
                        _scheduleState.value = ScheduleState.Error("Такая запись уже существует на эту дату")
                        return@launch // Прерываем, диалог не закроется
                    }
                }
            }

            // Если проверки пройдены (или это не перенос), обновляем запись
            val updatedAppointment = appointmentToUpdate.copy(
                status = newStatus,
                date = newDate ?: appointmentToUpdate.date // Используем новую дату, если она есть
            )
            appointmentDao.updateAppointment(updatedAppointment)
            _scheduleState.value = ScheduleState.Success
        }
    }

    fun resetScheduleState() {
        _scheduleState.value = ScheduleState.Idle
    }

    fun deleteAppointment(appointmentId: Long) {
        viewModelScope.launch {
            val appointmentToDelete = appointmentDao.getAppointmentById(appointmentId)
            appointmentToDelete?.let {
                appointmentDao.deleteAppointment(it)
            }
        }
    }

    // 1. Фрагмент вызывает это, когда открывает диалог
    fun onEditNoteClicked(appointmentId: Long) {
        pendingAppointmentId = appointmentId
    }

    // 2. Фрагмент вызывает это, когда диалог вернул результат
    fun onNoteDialogResult(newText: String) {
        val id = pendingAppointmentId ?: return // Если ID потерялся, ничего не делаем

        viewModelScope.launch {
            val appointment = appointmentDao.getAppointmentById(id) ?: return@launch
            val updatedAppointment = appointment.copy(note = newText)
            appointmentDao.updateAppointment(updatedAppointment)
        }

        pendingAppointmentId = null // Сбрасываем после сохранения
    }
}