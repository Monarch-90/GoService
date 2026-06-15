package com.avetiso.feature_statistics.domain.usecases

import com.avetiso.feature_statistics.StatisticsConstants
import com.avetiso.feature_statistics.domain.models.InventoryShortageItem
import com.avetiso.feature_statistics.domain.repository.StatisticsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * UseCase для получения списка заканчивающихся расходных материалов (Складской радар).
 * Выполняется на IO-диспетчере для безопасной работы с БД.
 */
class GetInventoryShortagesUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    /**
     * @return Список материалов с критическим остатком.
     */
    suspend fun execute(): List<InventoryShortageItem> = withContext(Dispatchers.IO) {
        // Запрашиваем из репозитория материалы, которые подходят к концу.
        // Ограничиваем выдачу 3 позициями, чтобы виджет на главном экране оставался компактным.
        return@withContext repository.getLowStockMaterials(limit = StatisticsConstants.Math.LIMIT)
    }
}