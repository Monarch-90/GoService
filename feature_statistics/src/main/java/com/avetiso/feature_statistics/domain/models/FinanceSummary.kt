package com.avetiso.feature_statistics.domain.models

import java.math.BigDecimal

/**
 * Доменная модель для финансовой сводки (Виджет 1).
 * Строго отдельный класс. Используется для передачи агрегированных данных из БД в UI.
 */
data class FinanceSummary(
    val totalRevenue: BigDecimal,
    val revenueTrendPercentage: Int, // Положительное число — рост (+15%), отрицательное — падение
    val averageCheck: BigDecimal,
    val servicesRenderedCount: Int
)