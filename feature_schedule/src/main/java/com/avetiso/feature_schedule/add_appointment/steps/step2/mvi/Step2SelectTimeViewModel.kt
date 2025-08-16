package com.avetiso.feature_schedule.add_appointment.steps.step2.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.TimeSlotDao
import com.avetiso.core.entity.TimeSlotEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Step2SelectTimeViewModel @Inject constructor(
    private val timeSlotDao: TimeSlotDao,
) : ViewModel() {

    // Получаем все слоты и подписываемся на их изменения
    val timeSlots: StateFlow<List<TimeSlotEntity>> = timeSlotDao.getAllTimeSlots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTimeSlot(hour: Int, minute: Int) {
        viewModelScope.launch {
            val totalMinutes = hour * 60 + minute
            // Проверяем, существует ли уже такой слот
            val exists = timeSlots.value.any { it.startTimeMinutes == totalMinutes }
            if (!exists) {
                timeSlotDao.insertTimeSlot(TimeSlotEntity(startTimeMinutes = totalMinutes))
            }
        }
    }

    fun updateTimeSlot(id: Long, hour: Int, minute: Int) {
        viewModelScope.launch {
            val totalMinutes = hour * 60 + minute
            val exists = timeSlots.value.any { it.startTimeMinutes == totalMinutes && it.id != id }
            if (!exists) {
                val updatedSlot = TimeSlotEntity(id = id, startTimeMinutes = totalMinutes)
                timeSlotDao.insertTimeSlot(updatedSlot)
            }
        }
    }

    fun deleteTimeSlot(timeSlot: TimeSlotEntity) {
        viewModelScope.launch {
            timeSlotDao.deleteTimeSlot(timeSlot)
        }
    }
}