package com.avetiso.feature_schedule.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.data.dao.ServiceDao
import com.avetiso.core.model.AppointmentWithDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val clientDao: ClientDao,
    private val serviceDao: ServiceDao,
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

                // 2. Собираем все уникальные ID клиентов и услуг
                val clientIds = appointments.map { it.clientId }.distinct()
                val serviceIds = appointments.flatMap { it.serviceIds }.distinct()

                // 3. Одним запросом получаем всех нужных клиентов и все нужные услуги
                val clients = clientDao.getClientsByIds(clientIds).associateBy { it.id }
                val services = serviceDao.getServicesByIds(serviceIds).associateBy { it.id }

                // 4. "Склеиваем" все данные в объекты AppointmentWithDetails
                appointments.mapNotNull { appointment ->
                    val client = clients[appointment.clientId]
                    val appointmentServices = appointment.serviceIds.mapNotNull { services[it] }

                    if (client != null) {
                        AppointmentWithDetails(
                            appointment = appointment,
                            client = client,
                            services = appointmentServices
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
}