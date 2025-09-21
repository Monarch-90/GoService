package com.avetiso.feature_schedule.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.entity.AppointmentEntity
import com.avetiso.core.model.ServiceSnapshot
import com.avetiso.feature_schedule.add_appointment.data.Appointment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val appointmentDao: AppointmentDao,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow("")
    private val gson = Gson()

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

    private fun mapToAppointment(appointmentEntity: AppointmentEntity): Appointment {
        val listType = object : TypeToken<List<ServiceSnapshot>>() {}.type
        val services: List<ServiceSnapshot> =
            gson.fromJson(appointmentEntity.servicesJson, listType) ?: emptyList()

        val hours = appointmentEntity.startTimeMinutes / 60
        val minutes = appointmentEntity.startTimeMinutes % 60
        val timeString = String.format("%02d:%02d", hours, minutes)

        val serviceNamesString = services.joinToString(", ") { it.name }

        val priceString = services
            .groupBy { it.currency }
            .map { (currency, servicesInCurrency) ->
                val total = servicesInCurrency.sumOf { it.price }
                val isPriceFrom = servicesInCurrency.any { it.isPriceFrom }
                val prefix = if (isPriceFrom) "от " else ""
                "$prefix${"%.2f".format(total)} $currency"
            }
            .joinToString("\n")

        return Appointment(
            id = appointmentEntity.id,
            time = timeString,
            serviceNames = serviceNamesString,
            clientName = appointmentEntity.clientName,
            price = priceString
        )
    }

    fun loadAppointmentsForDate(date: String) {
        _selectedDate.value = date
    }

    fun deleteAppointment(appointmentId: Long) {
        viewModelScope.launch {
            val appointmentToDelete = appointmentDao.getAppointmentById(appointmentId)
            appointmentToDelete?.let {
                appointmentDao.deleteAppointment(it)
            }
        }
    }
}