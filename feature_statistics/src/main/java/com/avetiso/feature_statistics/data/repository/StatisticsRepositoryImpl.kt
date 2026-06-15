package com.avetiso.feature_statistics.data.repository

import com.avetiso.core.AppConstants
import com.avetiso.core.data.dao.AppointmentDao
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.data.dao.ServiceDao
import com.avetiso.core.data.dao.TimeSlotDao
import com.avetiso.core.models.AppointmentStatus
import com.avetiso.core.models.ServiceSnapshot
import com.avetiso.core.usecase.CalculateServicePriceUseCase
import com.avetiso.feature_statistics.domain.models.InventoryShortageItem
import com.avetiso.feature_statistics.domain.models.ServiceItem
import com.avetiso.feature_statistics.domain.repository.StatisticsRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Реализация контракта репозитория статистики (Слой Data).
 * Извлекает данные из Core-модуля (Room DAO) и агрегирует их для статистики.
 */
class StatisticsRepositoryImpl @Inject constructor(
    private val appointmentDao: AppointmentDao,
    private val serviceDao: ServiceDao,
    private val clientDao: ClientDao,
    private val timeSlotDao: TimeSlotDao,
    private val calculatePriceUseCase: CalculateServicePriceUseCase
) : StatisticsRepository {

    private val gson = Gson()
    private val dbDateFormatter = SimpleDateFormat(AppConstants.Format.FULL_DATE_FORMAT, Locale.getDefault())

    // --- ФИНАНСЫ ---

    override suspend fun getRevenueBetween(startTimestamp: Long, endTimestamp: Long, currencyCode: String): BigDecimal = withContext(Dispatchers.IO) {
        val startDateStr = dbDateFormatter.format(Date(startTimestamp))
        val endDateStr = dbDateFormatter.format(Date(endTimestamp))

        val appointments = appointmentDao.getAppointmentsBetweenDatesSync(startDateStr, endDateStr)
        var totalRevenue = BigDecimal.ZERO

        // Идем только по исполненным записям
        appointments.filter { it.status == AppointmentStatus.COMPLETED }.forEach { appointment ->
            val listType = object : TypeToken<List<ServiceSnapshot>>() {}.type
            val services: List<ServiceSnapshot> = gson.fromJson(appointment.servicesJson, listType) ?: emptyList()

            // Просчитываем итоговую цену услуг с учетом скидки записи
            val calculationResults = calculatePriceUseCase(services, appointment.discountPercent)

            // Ищем сумму именно для запрашиваемой валюты
            val currencyTotal = calculationResults.find { it.currency == currencyCode }?.amount ?: 0.0
            totalRevenue = totalRevenue.add(BigDecimal.valueOf(currencyTotal))
        }

        return@withContext totalRevenue
    }

    override suspend fun getCompletedServicesCountBetween(startTimestamp: Long, endTimestamp: Long, currencyCode: String): Int = withContext(Dispatchers.IO) {
        val startDateStr = dbDateFormatter.format(Date(startTimestamp))
        val endDateStr = dbDateFormatter.format(Date(endTimestamp))

        val appointments = appointmentDao.getAppointmentsBetweenDatesSync(startDateStr, endDateStr)
        var servicesCount = 0

        appointments.filter { it.status == AppointmentStatus.COMPLETED }.forEach { appointment ->
            val listType = object : TypeToken<List<ServiceSnapshot>>() {}.type
            val services: List<ServiceSnapshot> = gson.fromJson(appointment.servicesJson, listType) ?: emptyList()

            // Считаем количество КОНКРЕТНЫХ УСЛУГ (а не записей), валюта которых совпадает с запрошенной
            servicesCount += services.count { it.currency == currencyCode }
        }

        return@withContext servicesCount
    }

    override suspend fun getAvailableCurrenciesBetween(startTimestamp: Long, endTimestamp: Long): Set<String> = withContext(Dispatchers.IO) {
        val startDateStr = dbDateFormatter.format(Date(startTimestamp))
        val endDateStr = dbDateFormatter.format(Date(endTimestamp))

        val appointments = appointmentDao.getAppointmentsBetweenDatesSync(startDateStr, endDateStr)
        val currencies = mutableSetOf<String>()

        appointments.filter { it.status == AppointmentStatus.COMPLETED }.forEach { appointment ->
            val listType = object : TypeToken<List<ServiceSnapshot>>() {}.type
            val services: List<ServiceSnapshot> = gson.fromJson(appointment.servicesJson, listType) ?: emptyList()

            // Собираем все уникальные валюты, которые были в этих записях
            services.forEach { service ->
                service.currency?.let { currencies.add(it) }
            }
        }

        return@withContext currencies
    }

// --- ЗАГРУЖЕННОСТЬ И КЛИЕНТЫ ---

    override suspend fun getCancellationsCountBetween(startTimestamp: Long, endTimestamp: Long): Int = withContext(Dispatchers.IO) {
        val startDateStr = dbDateFormatter.format(Date(startTimestamp))
        val endDateStr = dbDateFormatter.format(Date(endTimestamp))

        val appointments = appointmentDao.getAppointmentsBetweenDatesSync(startDateStr, endDateStr)

        return@withContext appointments.count { it.status == AppointmentStatus.CANCELLED }
    }

    override suspend fun getTotalWorkMinutesBetween(startTimestamp: Long, endTimestamp: Long): Int = withContext(Dispatchers.IO) {
        val startDateStr = dbDateFormatter.format(Date(startTimestamp))
        val endDateStr = dbDateFormatter.format(Date(endTimestamp))

        val appointments = appointmentDao.getAppointmentsBetweenDatesSync(startDateStr, endDateStr)

        // Время считаем с учетом количества выбранных слотов (по каждому слоту отрабатывается полное время услуги)
        return@withContext appointments
            .filter { it.status == AppointmentStatus.COMPLETED }
            .sumOf { it.totalDurationMinutes * maxOf(1, it.timeSlotIds.size) }
    }

    override suspend fun getNewClientsCountBetween(startTimestamp: Long, endTimestamp: Long): Int = withContext(Dispatchers.IO) {
        return@withContext clientDao.getNewClientsCount(startTimestamp, endTimestamp)
    }

    // --- ОСТАЛЬНЫЕ МЕТОДЫ (пока заглушки, обновим в процессе) ---

    override suspend fun getLowStockMaterials(limit: Int): List<InventoryShortageItem> {
        return emptyList()
    }

    override suspend fun getFreeTimeSlots(): List<String> {
        return emptyList()
    }

    override suspend fun getAllServices(): List<ServiceItem> {
        return emptyList()
    }
}