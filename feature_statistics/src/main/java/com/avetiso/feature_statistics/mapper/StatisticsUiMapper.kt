package com.avetiso.feature_statistics.mapper

import android.content.Context
import com.avetiso.feature_statistics.R
import com.avetiso.feature_statistics.StatisticsConstants
import com.avetiso.feature_statistics.models.FinanceCardItem
import com.avetiso.feature_statistics.models.InventoryShortUIItem
import com.avetiso.feature_statistics.models.InventoryWarningItem
import com.avetiso.feature_statistics.models.PeriodFilterItem
import com.avetiso.feature_statistics.models.QuickActionsItem
import com.avetiso.feature_statistics.models.StatisticsListItem
import com.avetiso.feature_statistics.models.TimePeriod
import com.avetiso.feature_statistics.models.WorkloadItem
import com.avetiso.feature_statistics.mvi.StatisticsState
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.NumberFormat
import java.util.Locale
import javax.inject.Inject

/**
 * Маппер для преобразования состояния экрана (StatisticsState) в плоский список UI-моделей.
 * Выполняет форматирование валют, дат и чисел строго до передачи в UI-слой (Adapter).
 */
class StatisticsUiMapper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // Используем обычный числовой форматер вместо валютного,
    // чтобы самостоятельно добавлять нужную валюту (GEL, USD) без жесткой привязки к рублю.
    private val numberFormatter = NumberFormat.getNumberInstance(Locale.getDefault()).apply {
        maximumFractionDigits = StatisticsConstants.Math.COUNT // Убираем копейки для красивого отображения на дашборде
    }

    /**
     * Конвертирует весь стейт в готовый список для RecyclerView.
     */
    fun mapToUiList(state: StatisticsState): List<StatisticsListItem> {
        val uiList = mutableListOf<StatisticsListItem>()

        // 1. Шапка (Фильтры периодов)
        uiList.add(mapPeriodFilter(state))

        // 2. Финансы (добавляем только если данные уже загружены)
        state.financeSummary?.let { finance ->
            // Безопасно берем текущую валюту. К моменту отрисовки финансов она уже 100% есть в стейте.
            val currency = state.selectedCurrency ?: ""

            uiList.add(
                FinanceCardItem(
                    // Форматируем число (например "5 000") и добавляем валюту ("GEL")
                    totalRevenue = "${numberFormatter.format(finance.totalRevenue)} $currency",
                    averageCheck = "${numberFormatter.format(finance.averageCheck)} $currency",
                    servicesCount = finance.servicesRenderedCount,
                    trendPercent = Math.abs(finance.revenueTrendPercentage),
                    isTrendPositive = finance.revenueTrendPercentage >= StatisticsConstants.Math.COUNT,

                    // Передаем данные для спиннера
                    selectedCurrency = currency,
                    availableCurrencies = state.availableCurrencies
                )
            )
        }

        // 3. Складской радар (показываем ВСЕГДА)
        val uiShortages = state.inventoryShortages.map { item ->
            InventoryShortUIItem(
                id = item.materialId,
                name = item.name,
                leftCount = item.remainingQuantity,
                measureUnit = item.unitMeasure
            )
        }
        uiList.add(InventoryWarningItem(uiShortages))

        // 4. Клиенты и Загруженность
        state.workloadSummary?.let { workload ->
            uiList.add(
                WorkloadItem(
                    newClientsCount = workload.newClientsCount,
                    cancellationsCount = workload.cancellationsCount,
                    // Конвертируем минуты из БД в часы для отображения в UI
                    totalWorkHours = workload.totalWorkMinutes / StatisticsConstants.Time.MINUTE
                )
            )
        }

        // 5. Быстрые действия (статичный блок в самом низу)
        uiList.add(QuickActionsItem)

        return uiList
    }

    private fun mapPeriodFilter(state: StatisticsState): PeriodFilterItem {
        val customRangeText = if (state.selectedPeriod == TimePeriod.CUSTOM && state.customDateStart != null && state.customDateEnd != null) {
            context.getString(R.string.statistics_filter_custom)
        } else {
            null
        }

        return PeriodFilterItem(
            selectedPeriod = state.selectedPeriod,
            customDateRange = customRangeText
        )
    }
}