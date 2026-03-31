package com.avetiso.feature_statistics.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.feature_statistics.R
import com.avetiso.feature_statistics.StatisticsConstants
import com.avetiso.feature_statistics.databinding.ItemStatisticsFinanceBinding
import com.avetiso.feature_statistics.databinding.ItemStatisticsInventoryBinding
import com.avetiso.feature_statistics.databinding.ItemStatisticsInventorySubItemBinding
import com.avetiso.feature_statistics.databinding.ItemStatisticsPeriodBinding
import com.avetiso.feature_statistics.databinding.ItemStatisticsQuickActionsBinding
import com.avetiso.feature_statistics.databinding.ItemStatisticsWorkloadBinding
import com.avetiso.feature_statistics.models.FinanceCardItem
import com.avetiso.feature_statistics.models.InventoryWarningItem
import com.avetiso.feature_statistics.models.PeriodFilterItem
import com.avetiso.feature_statistics.models.QuickActionsItem
import com.avetiso.feature_statistics.models.StatisticsListItem
import com.avetiso.feature_statistics.models.TimePeriod
import com.avetiso.feature_statistics.models.WorkloadItem
import com.avetiso.feature_statistics.mvi.OnFinanceCardClicked
import com.avetiso.feature_statistics.mvi.OnGenerateFreeWindowsClicked
import com.avetiso.feature_statistics.mvi.OnInventoryCopyToClipboardClicked
import com.avetiso.feature_statistics.mvi.OnInventorySeeAllClicked
import com.avetiso.feature_statistics.mvi.OnSharePriceListClicked
import com.avetiso.feature_statistics.mvi.OnWorkloadCardClicked
import com.avetiso.feature_statistics.mvi.SelectCurrency
import com.avetiso.feature_statistics.mvi.SelectPeriod
import com.avetiso.feature_statistics.mvi.StatisticsIntent

/**
 * Адаптер главного дашборда статистики.
 * Использует ListAdapter для автоматической анимации и вычисления разницы (DiffUtil).
 * Все действия пробрасываются в MVI через лямбду onIntent.
 */
class StatisticsAdapter(
    private val onIntent: (StatisticsIntent) -> Unit
) : ListAdapter<StatisticsListItem, RecyclerView.ViewHolder>(StatisticsDiffCallback()) {

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is PeriodFilterItem -> StatisticsConstants.ViewType.VIEW_TYPE_PERIOD
            is FinanceCardItem -> StatisticsConstants.ViewType.VIEW_TYPE_FINANCE
            is InventoryWarningItem -> StatisticsConstants.ViewType.VIEW_TYPE_INVENTORY
            is WorkloadItem -> StatisticsConstants.ViewType.VIEW_TYPE_WORKLOAD
            is QuickActionsItem -> StatisticsConstants.ViewType.VIEW_TYPE_QUICK_ACTIONS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            StatisticsConstants.ViewType.VIEW_TYPE_PERIOD -> PeriodViewHolder(
                ItemStatisticsPeriodBinding.inflate(inflater, parent, false), onIntent
            )
            StatisticsConstants.ViewType.VIEW_TYPE_FINANCE -> FinanceViewHolder(
                ItemStatisticsFinanceBinding.inflate(inflater, parent, false), onIntent
            )
            StatisticsConstants.ViewType.VIEW_TYPE_INVENTORY -> InventoryViewHolder(
                ItemStatisticsInventoryBinding.inflate(inflater, parent, false), onIntent
            )
            StatisticsConstants.ViewType.VIEW_TYPE_WORKLOAD -> WorkloadViewHolder(
                ItemStatisticsWorkloadBinding.inflate(inflater, parent, false), onIntent
            )
            StatisticsConstants.ViewType.VIEW_TYPE_QUICK_ACTIONS -> QuickActionsViewHolder(
                ItemStatisticsQuickActionsBinding.inflate(inflater, parent, false), onIntent
            )
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is PeriodViewHolder -> holder.bind(item as PeriodFilterItem)
            is FinanceViewHolder -> holder.bind(item as FinanceCardItem)
            is InventoryViewHolder -> holder.bind(item as InventoryWarningItem)
            is WorkloadViewHolder -> holder.bind(item as WorkloadItem)
            is QuickActionsViewHolder -> holder.bind()
        }
    }
}

// --- DiffUtil и ViewHolders строго вынесены на верхний уровень (SOLID) ---

class StatisticsDiffCallback : DiffUtil.ItemCallback<StatisticsListItem>() {
    override fun areItemsTheSame(oldItem: StatisticsListItem, newItem: StatisticsListItem): Boolean {
        // Так как каждый виджет присутствует на экране в единственном экземпляре,
        // принадлежность к одному классу означает, что это тот же самый виджет.
        return oldItem::class == newItem::class
    }

    override fun areContentsTheSame(oldItem: StatisticsListItem, newItem: StatisticsListItem): Boolean {
        return oldItem == newItem
    }
}

class PeriodViewHolder(
    private val binding: ItemStatisticsPeriodBinding,
    private val onIntent: (StatisticsIntent) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: PeriodFilterItem) {
        // Здесь должна быть логика визуального выделения Chip-а на основе item.selectedPeriod
        // Отправка интентов при клике:
        binding.chipToday.setOnClickListener { onIntent(SelectPeriod(TimePeriod.TODAY)) }
        binding.chipWeek.setOnClickListener { onIntent(SelectPeriod(TimePeriod.WEEK)) }
        binding.chipMonth.setOnClickListener { onIntent(SelectPeriod(TimePeriod.MONTH)) }
        binding.chipCustom.setOnClickListener {
            // Интент вызова DatePickerDialog будет обработан во ViewModel/Fragment
        }
    }
}

class FinanceViewHolder(
    private val binding: ItemStatisticsFinanceBinding,
    private val onIntent: (StatisticsIntent) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        // Устанавливаем клик на саму карточку (но игнорируем спиннер, у него свои события)
        binding.root.setOnClickListener { onIntent(OnFinanceCardClicked) }
    }

    fun bind(item: FinanceCardItem) {
        val context = binding.root.context
        binding.tvTotalRevenue.text = item.totalRevenue
        binding.tvAverageCheck.text = context.getString(R.string.statistics_finance_average_check, item.averageCheck)
        binding.tvServicesCount.text = context.getString(R.string.statistics_finance_services_count, item.servicesCount)

        val trendRes = if (item.isTrendPositive) R.string.statistics_finance_trend_positive else R.string.statistics_finance_trend_negative
        binding.tvTrend.text = context.getString(trendRes, item.trendPercent)

        val iconRes = if (item.isTrendPositive) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down
        binding.ivTrendIcon.setImageResource(iconRes)

        // --- Настройка Спиннера (Мультивалютность) ---

        // 1. Отключаем листенер перед биндингом во избежание ложных срабатываний
        binding.spinnerCurrency.onItemSelectedListener = null

        // 2. Инициализируем адаптер с системным красивым UI
        val adapter = ArrayAdapter(
            context,
            android.R.layout.simple_spinner_item,
            item.availableCurrencies
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = adapter

        // 3. Выставляем выбранную валюту без анимации (false)
        val selectedIndex = item.availableCurrencies.indexOf(item.selectedCurrency)
        if (selectedIndex >= 0) {
            binding.spinnerCurrency.setSelection(selectedIndex, false)
        }

        // 4. Вешаем листенер обратно
        binding.spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = item.availableCurrencies[position]
                // Строгая защита: отправляем интент только если пользователь реально выбрал ДРУГУЮ валюту
                if (selected != item.selectedCurrency) {
                    onIntent(SelectCurrency(selected))
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
}

class InventoryViewHolder(
    private val binding: ItemStatisticsInventoryBinding,
    private val onIntent: (StatisticsIntent) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.btnSeeAll.setOnClickListener { onIntent(OnInventorySeeAllClicked) }
        binding.btnToShoppingList.setOnClickListener { onIntent(OnInventoryCopyToClipboardClicked) }
    }

    fun bind(item: InventoryWarningItem) {
        val context = binding.root.context
        val inflater = LayoutInflater.from(context)

        // Очищаем контейнер перед биндингом, чтобы избежать дублирования при скролле (переиспользовании ViewHolder)
        binding.llItemsContainer.removeAllViews()

        item.items.forEach { shortItem ->
            // Динамически инфлейтим подпункты в LinearLayout — это правильный Enterprise-подход
            // для маленьких вложенных списков (2-3 элемента), вместо тяжелого вложенного RecyclerView.
            val subBinding = ItemStatisticsInventorySubItemBinding.inflate(inflater, binding.llItemsContainer, true)
            subBinding.tvItemName.text = shortItem.name
            subBinding.tvItemLeft.text = context.getString(
                R.string.statistics_inventory_item_left,
                shortItem.leftCount,
                shortItem.measureUnit
            )
        }
    }
}

class WorkloadViewHolder(
    private val binding: ItemStatisticsWorkloadBinding,
    private val onIntent: (StatisticsIntent) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.root.setOnClickListener { onIntent(OnWorkloadCardClicked) }
    }

    fun bind(item: WorkloadItem) {
        val context = binding.root.context
        binding.tvNewClientsCount.text = item.newClientsCount.toString()
        binding.tvCancellationsCount.text = item.cancellationsCount.toString()
        binding.tvWorkHours.text = context.getString(R.string.statistics_load_work_hours_value, item.totalWorkHours)
    }
}

class QuickActionsViewHolder(
    private val binding: ItemStatisticsQuickActionsBinding,
    private val onIntent: (StatisticsIntent) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.btnFreeWindows.setOnClickListener { onIntent(OnGenerateFreeWindowsClicked) }
        binding.btnSharePrice.setOnClickListener { onIntent(OnSharePriceListClicked) }
    }

    fun bind() {
        // Статичный UI, не требует передачи данных в биндинг
    }
}