package com.avetiso.feature_statistics.domain.usecases

import android.content.Context
import com.avetiso.feature_statistics.R
import com.avetiso.feature_statistics.domain.models.InventoryShortageItem
import com.avetiso.feature_statistics.domain.models.TimePeriod
import com.avetiso.feature_statistics.domain.repository.StatisticsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.StringBuilder
import javax.inject.Inject

/**
 * UseCase для генерации текстов для буфера обмена (Быстрые действия).
 * Строго соблюдает правило отсутствия захардкоженных строк: все тексты и форматы
 * берутся исключительно из ресурсов (strings.xml).
 */
class GenerateStatisticsTextUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: StatisticsRepository
) {
    /**
     * Генерирует список покупок на основе заканчивающихся материалов.
     */
    suspend fun generateInventoryShoppingList(
        shortages: List<InventoryShortageItem>
    ): String = withContext(Dispatchers.Default) {
        if (shortages.isEmpty()) {
            return@withContext context.getString(R.string.empty_shopping_list)
        }

        val builder = StringBuilder()
        builder.append(context.getString(R.string.shopping_list_header)).append("\n\n")

        shortages.forEachIndexed { index, item ->
            // Формат в XML должен быть: "%1$d. %2$s — осталось: %3$d %4$s"
            val line = context.getString(
                R.string.shopping_list_item_format,
                index + 1,
                item.name,
                item.remainingQuantity,
                item.unitMeasure
            )
            builder.append(line).append("\n")
        }

        return@withContext builder.toString()
    }

    /**
     * Генерирует текст со свободными окнами для Instagram/соцсетей.
     */
    suspend fun generateFreeWindows(period: TimePeriod): String = withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        builder.append(context.getString(R.string.free_windows_header)).append("\n\n")

        // Запрашиваем реальные свободные слоты из БД через репозиторий
        val freeSlots = repository.getFreeTimeSlots()

        if (freeSlots.isEmpty()) {
            return@withContext context.getString(R.string.no_free_windows)
        }

        freeSlots.forEach { slot ->
            // Формат в XML: "• %1$s" (где slot - уже отформатированная строка времени)
            val line = context.getString(R.string.free_windows_item_format, slot)
            builder.append(line).append("\n")
        }

        return@withContext builder.toString()
    }

    /**
     * Генерирует прайс-лист всех услуг мастера.
     */
    suspend fun generatePriceList(): String = withContext(Dispatchers.IO) {
        val builder = StringBuilder()
        builder.append(context.getString(R.string.price_list_header)).append("\n\n")

        // Запрашиваем реальный список услуг из БД
        val services = repository.getAllServices()

        if (services.isEmpty()) {
            return@withContext context.getString(R.string.empty_price_list)
        }

        services.forEach { service ->
            // Формат в XML: "%1$s — %2$s ₽" (где price конвертируется в читаемый вид)
            val line = context.getString(
                R.string.price_list_item_format,
                service.name,
                service.price.toPlainString()
            )
            builder.append(line).append("\n")
        }

        return@withContext builder.toString()
    }
}