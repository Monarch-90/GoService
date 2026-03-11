package com.avetiso.feature_statistics.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.feature_statistics.models.StatisticsListItem
import com.avetiso.feature_statistics.models.TimePeriod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    // TODO: Позже заинжектим здесь UseCase-ы для запросов к БД (AppointmentDao, ClientDao и т.д.)
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState.initial())
    val state = _state.asStateFlow()

    private val _events = Channel<StatisticsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        // При запуске экрана сразу загружаем статистику за "Сегодня"
        loadStatistics(TimePeriod.TODAY)
    }

    fun processIntent(intent: StatisticsIntent) {
        when (intent) {
            is StatisticsIntent.ChangePeriod -> {
                // Избегаем лишних запросов, если период не изменился
                if (_state.value.selectedPeriod != intent.period) {
                    loadStatistics(intent.period)
                }
            }

            is StatisticsIntent.OpenCustomDateSelector -> {
                viewModelScope.launch {
                    _events.send(StatisticsEvent.NavigateToCustomDateRangePicker)
                }
            }

            is StatisticsIntent.OnFinancialCardClicked -> {
                viewModelScope.launch {
                    _events.send(StatisticsEvent.NavigateToFinancialDetails)
                }
            }

            is StatisticsIntent.OnInventorySeeAllClicked -> {
                viewModelScope.launch {
                    _events.send(StatisticsEvent.NavigateToInventory)
                }
            }

            is StatisticsIntent.OnInventoryCopyToClipboardClicked -> {
                viewModelScope.launch {
                    _events.send(StatisticsEvent.CopyToClipboard(generateInventoryTextForClipboard()))
                }
            }

            is StatisticsIntent.OnClientsLoadClicked -> {
                viewModelScope.launch {
                    _events.send(StatisticsEvent.NavigateToClientsStats)
                }
            }

            is StatisticsIntent.OnGenerateFreeSlotsClicked -> {
                viewModelScope.launch {
                    // TODO: Реализуем логику генерации текста свободных окон
                }
            }

            is StatisticsIntent.OnSharePriceClicked -> {
                viewModelScope.launch {
                    // TODO: Реализуем логику генерации прайса-листа
                }
            }
        }
    }

    private fun loadStatistics(period: TimePeriod, customRange: String? = null) {
        _state.update { it.copy(isLoading = true, selectedPeriod = period) }

        viewModelScope.launch {
            // TODO: Здесь будет сбор данных из БД (combine flow из разных таблиц).
            // Ниже временно формируем структуру из мок-данных для верстки и настройки адаптера.

            val items = mutableListOf<StatisticsListItem>()

            // 1. Шапка (Выбор периода)
            items.add(
                StatisticsListItem.PeriodFilter(
                    selectedPeriod = period,
                    customDateRange = customRange
                )
            )

            // 2. Финансы
            items.add(
                StatisticsListItem.FinanceCard(
                    totalRevenue = "125 000 GEL", // Валюту позже подтянем из SettingsRepository
                    averageCheck = "2 500 GEL",
                    servicesCount = 42,
                    trendPercent = 15,
                    isTrendPositive = true
                )
            )

            // 3. Склад (Добавляем в список ТОЛЬКО если есть что-то заканчивающееся)
            // if (inventoryItems.isNotEmpty()) { ... }
            items.add(
                StatisticsListItem.InventoryWarning(
                    items = listOf(
                        StatisticsListItem.InventoryWarning.InventoryShortItem(1L, "Перчатки нитриловые", 1, "уп."),
                        StatisticsListItem.InventoryWarning.InventoryShortItem(2L, "Краска Estel 5.0", 0, "шт.")
                    )
                )
            )

            // 4. Загруженность и клиенты
            items.add(
                StatisticsListItem.Workload(
                    newClientsCount = 5,
                    cancellationsCount = 2,
                    totalWorkHours = 48
                )
            )

            // 5. Быстрые действия
            items.add(StatisticsListItem.QuickActions)

            // Обновляем State
            _state.update {
                it.copy(
                    isLoading = false,
                    dashboardItems = items
                )
            }
        }
    }

    private fun generateInventoryTextForClipboard(): String {
        // TODO: Генерация реального текста на основе БД
        return "Заканчиваются материалы:\n- Перчатки нитриловые (остаток: 1 уп.)\n- Краска Estel 5.0 (остаток: 0 шт.)"
    }
}