package com.avetiso.feature_statistics.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.repository.SettingsRepository
import com.avetiso.feature_statistics.R
import com.avetiso.feature_statistics.models.TimePeriod
import com.avetiso.feature_statistics.usecases.GenerateStatisticsTextUseCase
import com.avetiso.feature_statistics.usecases.GetAvailableCurrenciesUseCase
import com.avetiso.feature_statistics.usecases.GetFinanceSummaryUseCase
import com.avetiso.feature_statistics.usecases.GetInventoryShortagesUseCase
import com.avetiso.feature_statistics.usecases.GetWorkloadSummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVI ViewModel для экрана статистики.
 * Выступает как Presentation Layer: запрашивает чистые данные у UseCase-ов
 * и адаптирует их для безопасного отображения в UI.
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getFinanceSummaryUseCase: GetFinanceSummaryUseCase,
    private val getAvailableCurrenciesUseCase: GetAvailableCurrenciesUseCase,
    private val getInventoryShortagesUseCase: GetInventoryShortagesUseCase,
    private val getWorkloadSummaryUseCase: GetWorkloadSummaryUseCase,
    private val generateTextUseCase: GenerateStatisticsTextUseCase,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(StatisticsState())
    val state: StateFlow<StatisticsState> = _state.asStateFlow()

    private val _event = Channel<StatisticsEvent>(Channel.BUFFERED)
    val event = _event.receiveAsFlow()

    init {
        // При старте экрана: берем валюту по умолчанию, пишем в стейт, грузим "Сегодня"
        viewModelScope.launch {
            val defaultCurrency = settingsRepository.defaultCurrency.first()
            _state.update { it.copy(selectedCurrency = defaultCurrency) }
            handleSelectPeriod(TimePeriod.TODAY)
        }
    }

    fun processIntent(intent: StatisticsIntent) {
        when (intent) {
            is OnScreenResumed -> handleScreenResumed()

            is SelectPeriod -> handleSelectPeriod(intent.period)
            is SelectCustomPeriod -> handleSelectCustomPeriod(intent.startDateTimestamp, intent.endDateTimestamp)
            is OnCustomDateClick -> sendEvent(ShowDateRangePicker)
            is SelectCurrency -> handleSelectCurrency(intent.currencyCode)

            is OnFinanceCardClicked -> sendEvent(NavigateToCompletedAppointments)
            is OnWorkloadCardClicked -> sendEvent(NavigateToFrequentClients)
            is OnInventorySeeAllClicked -> sendEvent(NavigateToInventory)

            is OnInventoryCopyToClipboardClicked -> handleCopyInventory()
            is OnGenerateFreeWindowsClicked -> handleGenerateFreeWindows()
            is OnSharePriceListClicked -> handleSharePriceList()
        }
    }

    private fun handleScreenResumed() {
        loadDataForPeriod(
            period = _state.value.selectedPeriod,
            customStart = _state.value.customDateStart,
            customEnd = _state.value.customDateEnd
        )
    }
    private fun handleSelectCurrency(currencyCode: String) {
        if (_state.value.selectedCurrency == currencyCode) return

        _state.update { it.copy(selectedCurrency = currencyCode, isLoading = true) }
        loadDataForPeriod(
            period = _state.value.selectedPeriod,
            customStart = _state.value.customDateStart,
            customEnd = _state.value.customDateEnd
        )
    }

    private fun handleSelectPeriod(period: TimePeriod) {
        _state.update { it.copy(selectedPeriod = period, isLoading = true) }
        loadDataForPeriod(period = period, customStart = null, customEnd = null)
    }

    private fun handleSelectCustomPeriod(start: Long, end: Long) {
        _state.update {
            it.copy(
                selectedPeriod = TimePeriod.CUSTOM,
                customDateStart = start,
                customDateEnd = end,
                isLoading = true
            )
        }
        loadDataForPeriod(period = TimePeriod.CUSTOM, customStart = start, customEnd = end)
    }

    private fun loadDataForPeriod(period: TimePeriod, customStart: Long?, customEnd: Long?) {
        viewModelScope.launch {
            val currentCurrency = _state.value.selectedCurrency ?: return@launch

            // 1. Получаем чистый список валют, в которых БЫЛИ доходы (Слой Domain)
            val fetchedCurrencies = getAvailableCurrenciesUseCase.execute(period, customStart, customEnd)

            // 2. Логика Presentation: гарантируем, что текущая выбранная валюта
            // всегда есть в списке для Спиннера, чтобы не сломать UI.
            val displayCurrencies = if (fetchedCurrencies.contains(currentCurrency)) {
                fetchedCurrencies
            } else {
                (fetchedCurrencies + currentCurrency).sorted()
            }

            // 3. Запрашиваем остальные данные (финансы считаются по текущей валюте)
            val finance = getFinanceSummaryUseCase.execute(period, currentCurrency, customStart, customEnd)
            val inventory = getInventoryShortagesUseCase.execute()
            val workload = getWorkloadSummaryUseCase.execute(period, customStart, customEnd)

            _state.update {
                it.copy(
                    isLoading = false,
                    availableCurrencies = displayCurrencies,
                    financeSummary = finance,
                    inventoryShortages = inventory,
                    workloadSummary = workload
                )
            }
        }
    }

    private fun handleCopyInventory() {
        viewModelScope.launch {
            val textToCopy = generateTextUseCase.generateInventoryShoppingList(state.value.inventoryShortages)
            sendEvent(CopyToClipboard(textToCopy, R.string.success_copied_inventory))
        }
    }

    private fun handleGenerateFreeWindows() {
        viewModelScope.launch {
            val textToCopy = generateTextUseCase.generateFreeWindows(state.value.selectedPeriod)
            sendEvent(CopyToClipboard(textToCopy, R.string.success_copied_free_windows))
        }
    }

    private fun handleSharePriceList() {
        viewModelScope.launch {
            val textToCopy = generateTextUseCase.generatePriceList()
            sendEvent(CopyToClipboard(textToCopy, R.string.success_copied_price_list))
        }
    }

    private fun sendEvent(event: StatisticsEvent) {
        viewModelScope.launch {
            _event.send(event)
        }
    }
}