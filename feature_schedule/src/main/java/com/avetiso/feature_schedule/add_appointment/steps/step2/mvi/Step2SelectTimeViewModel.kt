package com.avetiso.feature_schedule.add_appointment.steps.step2.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.TimeSlotDao
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.core.entity.TimeSlotEntity
import com.avetiso.core.model.UiText
import com.avetiso.feature_schedule.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _eventChannel = Channel<Step2Event>()
    val events = _eventChannel.receiveAsFlow()

    private var timeSlotPendingDelete: TimeSlotEntity? = null

    fun saveTimeSlot(hour: Int, minute: Int, id: Long = 0L) {
        viewModelScope.launch {
            val totalMinutes = hour * 60 + minute

            // УМНАЯ ПРОВЕРКА:
            // Ищем слот с таким же временем, у которого ID НЕ совпадает с текущим.
            // 1. При добавлении (id=0): найдет любой слот с таким временем.
            // 2. При обновлении (id=5): найдет чужой слот, но проигнорирует "самого себя".
            val isDuplicate = timeSlots.value.any {
                it.startTimeMinutes == totalMinutes && it.id != id
            }

            if (isDuplicate) {
                // Текст "Такой слот..." вынеси в ресурсы позже
                _eventChannel.send(
                    Step2Event.ShowToast(
                        UiText.StringResource(R.string.Такой_слот_уже_существует
                        )
                    )
                )
            } else {
                // Room сам разберется: если id=0 -> INSERT, если id>0 -> UPDATE (при OnConflictStrategy.REPLACE)
                timeSlotDao.insertTimeSlot(TimeSlotEntity(id = id, startTimeMinutes = totalMinutes))
            }
        }
    }

    // 1. Нажали на урну
    fun onDeleteIconClicked(timeSlot: TimeSlotEntity) {
        timeSlotPendingDelete = timeSlot
    }

    // 2. Подтвердили в диалоге
    fun onDeleteConfirmed() {
        val timeSlot = timeSlotPendingDelete ?: return
        viewModelScope.launch {
            // Предполагается, что serviceDao у тебя есть в конструкторе
            timeSlotDao.deleteTimeSlot(timeSlot)
        }
        timeSlotPendingDelete = null
    }
}