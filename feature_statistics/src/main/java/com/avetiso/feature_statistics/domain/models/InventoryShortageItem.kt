package com.avetiso.feature_statistics.domain.models

/**
 * Доменная модель для конкретного материала/товара, который заканчивается (Виджет 2).
 * Строго отдельный класс без вложенности.
 */
data class InventoryShortageItem(
    val materialId: Long,
    val name: String,
    val remainingQuantity: Int,
    val unitMeasure: String // Например: "уп.", "шт.", "мл" - будет мапиться из ресурсов/БД
)