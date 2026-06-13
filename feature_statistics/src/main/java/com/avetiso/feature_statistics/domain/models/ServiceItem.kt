package com.avetiso.feature_statistics.domain.models

import java.math.BigDecimal

/**
 * Доменная модель услуги мастера (используется для генерации прайс-листа и аналитики).
 * Строго отдельный класс без вложенности.
 */
data class ServiceItem(
    val id: Long,
    val name: String,
    val price: BigDecimal,
    val durationMinutes: Int // Храним в минутах для максимальной точности при расчетах времени
)