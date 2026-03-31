package com.avetiso.feature_statistics.models

/**
 * Доменная модель для оценки плотности расписания и загруженности мастера (Виджет 3).
 * Строго отдельный класс, независимый от UI.
 */
data class WorkloadSummary(
    val newClientsCount: Int,
    val cancellationsCount: Int,
    val totalWorkMinutes: Int
)