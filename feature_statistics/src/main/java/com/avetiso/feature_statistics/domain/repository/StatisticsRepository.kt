package com.avetiso.feature_statistics.domain.repository

import com.avetiso.feature_statistics.domain.models.InventoryShortageItem
import com.avetiso.feature_statistics.domain.models.ServiceItem
import java.math.BigDecimal

/**
 * Контракт репозитория для модуля статистики.
 * Изолирует бизнес-логику (UseCase) от слоя данных (Room DAO).
 * Все методы suspend, так как реализация будет ходить в базу данных.
 */
interface StatisticsRepository {

    // --- Финансы ---

    /**
     * Возвращает общую сумму дохода за указанный период в конкретной валюте.
     */
    suspend fun getRevenueBetween(startTimestamp: Long, endTimestamp: Long, currencyCode: String): BigDecimal

    /**
     * Возвращает количество успешно оказанных услуг за период в конкретной валюте.
     */
    suspend fun getCompletedServicesCountBetween(startTimestamp: Long, endTimestamp: Long, currencyCode: String): Int

    /**
     * Сканирует исполненные записи за период и возвращает уникальный набор (Set) кодов валют,
     * которые в них использовались.
     */
    suspend fun getAvailableCurrenciesBetween(startTimestamp: Long, endTimestamp: Long): Set<String>


    // --- Склад / Инвентарь ---

    suspend fun getLowStockMaterials(limit: Int): List<InventoryShortageItem>


    // --- Загруженность и Клиенты ---

    suspend fun getNewClientsCountBetween(startTimestamp: Long, endTimestamp: Long): Int
    suspend fun getCancellationsCountBetween(startTimestamp: Long, endTimestamp: Long): Int
    suspend fun getTotalWorkMinutesBetween(startTimestamp: Long, endTimestamp: Long): Int


    // --- Быстрые действия ---

    suspend fun getFreeTimeSlots(): List<String>
    suspend fun getAllServices(): List<ServiceItem>
}