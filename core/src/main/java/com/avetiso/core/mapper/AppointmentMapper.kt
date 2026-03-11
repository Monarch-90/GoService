package com.avetiso.core.mapper

import com.avetiso.core.data.dao.TimeSlotDao
import com.avetiso.core.entity.AppointmentEntity
import com.avetiso.core.entity.ui.Appointment
import com.avetiso.core.models.ServiceSnapshot
import com.avetiso.core.usecase.CalculateServicePriceUseCase
import com.avetiso.core.utils.formattedTime
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import javax.inject.Inject

class AppointmentMapper @Inject constructor(
    private val timeSlotDao: TimeSlotDao,
    private val calculatePriceUseCase: CalculateServicePriceUseCase,
    private val uiPriceMapper: AppointmentPriceMapper,
) {
    private val gson = Gson()
    private val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

    suspend fun mapToUiModel(entity: AppointmentEntity): Appointment = withContext(Dispatchers.IO) {

        android.util.Log.d("AppTrace", "Mapper: mapToUiModel STARTED for ID = ${entity.id} on thread = ${Thread.currentThread().name}")

        val listType = object : TypeToken<List<ServiceSnapshot>>() {}.type
        val services: List<ServiceSnapshot> = gson.fromJson(entity.servicesJson, listType) ?: emptyList()

        // 1. ЗАГРУЖАЕМ ВСЕ ОБЪЕКТЫ СЛОТОВ ИЗ БД ПО ID
        val timeSlots = timeSlotDao.getTimeSlotsByIds(entity.timeSlotIds)
            .sortedBy { it.startTimeMinutes }

        // 2. ФОРМАТИРУЕМ КАЖДЫЙ СЛОТ И ОБЪЕДИНЯЕМ В ОДНУ СТРОКУ
        val timeString = timeSlots.joinToString(separator = "\n") { it.formattedTime }

        val serviceNamesString = services.joinToString(", ") { it.name }

        // БЕЗОПАСНЫЙ ПАРСИНГ (Defensive Programming)
        val formattedDate = try {
            if (entity.date.isBlank()) {
                "" // Если дата пустая, просто возвращаем пустую строку (или дефолтный текст)
            } else {
                val rawDate = LocalDate.parse(entity.date)
                rawDate.format(dateFormatter)
            }
        } catch (e: Exception) {
            // Если формат сломан, отдаем как есть, чтобы UI не падал
            entity.date
        }

        val calculationResults = calculatePriceUseCase(services, entity.discountPercent)
        val priceString = uiPriceMapper.mapToString(calculationResults)

        val result = Appointment(
            id = entity.id,
            time = timeString,
            date = formattedDate,
            serviceNames = serviceNamesString,
            clientName = entity.clientName,
            price = priceString,
            hasDiscount = entity.discountPercent > 0,
            status = entity.status,
            note = entity.note,
        )

        android.util.Log.d("AppTrace", "Mapper: mapToUiModel FINISHED for ID = ${entity.id}")
        result
    }
}