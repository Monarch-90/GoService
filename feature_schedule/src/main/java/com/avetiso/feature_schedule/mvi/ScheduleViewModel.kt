package com.avetiso.feature_schedule.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.data.dao.ServiceDao
import com.avetiso.core.data.dao.TimeSlotDao
import com.avetiso.core.model.AppointmentWithDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val clientDao: ClientDao,
    private val serviceDao: ServiceDao,
    private val timeSlotDao: TimeSlotDao,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow("")

    val appointmentsForDate: StateFlow<List<AppointmentWithDetails>> = _selectedDate
        .flatMapLatest { date ->
            if (date.isBlank()) {
                return@flatMapLatest flowOf(emptyList())
            }
            // 1. Получаем основные данные о записях
            appointmentDao.getAppointmentsForDate(date).map { appointments ->
                if (appointments.isEmpty()) {
                    return@map emptyList()
                }

                // 2. Собираем все уникальные ID клиентов, услуг и слотов
                val clientIds = appointments.map { it.clientId }.distinct()
                val serviceIds = appointments.flatMap { it.serviceIds }.distinct()
                val timeSlotIds = appointments.map { it.timeSlotId }.distinct()

                // 3. Одним запросом получаем всех нужных клиентов, услуги и слоты
                val clients = clientDao.getClientsByIds(clientIds).associateBy { it.id }
                val services = serviceDao.getServicesByIds(serviceIds).associateBy { it.id }
                val timeSlots = timeSlotDao.getTimeSlotsByIds(timeSlotIds).associateBy { it.id }

                // 4. "Склеиваем" все данные в объекты AppointmentWithDetails
                appointments.mapNotNull { appointment ->
                    val client = clients[appointment.clientId]
                    val appointmentServices = appointment.serviceIds.mapNotNull { services[it] }
                    val timeSlot = timeSlots[appointment.timeSlotId]

                    if (client != null && timeSlot != null) {
                        AppointmentWithDetails(
                            appointment = appointment,
                            client = client,
                            services = appointmentServices,
                            timeSlot = timeSlot
                        )
                    } else {
                        null // Если клиент не найден, пропускаем запись
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadAppointmentsForDate(date: String) {
        _selectedDate.value = date
    }

    fun deleteAppointment(appointmentId: Long) {
        viewModelScope.launch {
            // Сначала находим запись в БД по ID
            val appointmentToDelete = appointmentDao.getAppointmentById(appointmentId)
            // Если нашли - удаляем
            appointmentToDelete?.let {
                appointmentDao.deleteAppointment(it)
            }
        }
    }
}