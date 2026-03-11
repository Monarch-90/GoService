package com.avetiso.core.action

import com.avetiso.core.R
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.models.AppointmentStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AppointmentActionHandler @Inject constructor(
    private val appointmentDao: AppointmentDao
) {
    // Состояние для диалогов, которое переживет поворот экрана внутри ViewModel
    var pendingAppointmentId: Long? = null
    var appointmentPendingDeleteId: Long? = null

    suspend fun updateAppointmentStatus(
        appointmentId: Long,
        newStatus: AppointmentStatus,
        newDate: String? = null
    ): AppointmentActionResult = withContext(Dispatchers.IO) {
        val appointmentToUpdate = appointmentDao.getAppointmentById(appointmentId)
            ?: return@withContext AppointmentActionResult.Success

        // ПРОВЕРЯЕМ, НЕ ЯВЛЯЕТСЯ ЛИ ПЕРЕНОС "ФИКТИВНЫМ" (НА ТУ ЖЕ ДАТУ)
        val isReschedulingToSameDate = newDate != null && newDate == appointmentToUpdate.date

        if (newDate != null && !isReschedulingToSameDate) {
            val existingAppointmentsOnNewDate = appointmentDao.getAppointmentsForDateSync(newDate)
            val isDuplicate = existingAppointmentsOnNewDate.any {
                it.timeSlotIds.sorted() == appointmentToUpdate.timeSlotIds.sorted() &&
                        it.clientName == appointmentToUpdate.clientName &&
                        it.clientPhoneNumber == appointmentToUpdate.clientPhoneNumber &&
                        it.clientInstagram == appointmentToUpdate.clientInstagram &&
                        it.servicesJson == appointmentToUpdate.servicesJson
            }

            if (isDuplicate) {
                return@withContext AppointmentActionResult.Error(R.string.Такая_запись_уже_существует_на_эту_дату)
            }
        }

        // Если проверки пройдены (или это не перенос), обновляем запись
        val updatedAppointment = appointmentToUpdate.copy(
            status = newStatus,
            date = newDate ?: appointmentToUpdate.date // Используем новую дату, если она есть
        )
        appointmentDao.updateAppointment(updatedAppointment)

        if (newDate != null && !isReschedulingToSameDate) {
            AppointmentActionResult.RescheduleSuccess
        } else {
            AppointmentActionResult.Success
        }
    }

    // Подтвердили удаление
    suspend fun confirmDelete() = withContext(Dispatchers.IO) {
        val id = appointmentPendingDeleteId ?: return@withContext
        val entity = appointmentDao.getAppointmentById(id)
        if (entity != null) {
            appointmentDao.deleteAppointment(entity)
        }
        appointmentPendingDeleteId = null
    }

    // Фрагмент вызывает это, когда диалог вернул результат
    suspend fun updateNote(newText: String) = withContext(Dispatchers.IO) {
        val id = pendingAppointmentId ?: return@withContext // Если ID потерялся, ничего не делаем
        val appointment = appointmentDao.getAppointmentById(id) ?: return@withContext
        val updatedAppointment = appointment.copy(note = newText)
        appointmentDao.updateAppointment(updatedAppointment)
        pendingAppointmentId = null // Сбрасываем после сохранения
    }
}

sealed interface AppointmentActionResult {
    data object Success : AppointmentActionResult
    data object RescheduleSuccess : AppointmentActionResult
    data class Error(val messageResId: Int) : AppointmentActionResult
}