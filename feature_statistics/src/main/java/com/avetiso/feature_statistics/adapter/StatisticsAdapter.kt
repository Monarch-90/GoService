package com.avetiso.feature_statistics.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.feature_statistics.R
import com.avetiso.feature_statistics.databinding.ItemStatisticsFinanceBinding
import com.avetiso.feature_statistics.databinding.ItemStatisticsInventoryBinding
import com.avetiso.feature_statistics.databinding.ItemStatisticsInventorySubItemBinding
import com.avetiso.feature_statistics.databinding.ItemStatisticsPeriodBinding
import com.avetiso.feature_statistics.databinding.ItemStatisticsQuickActionsBinding
import com.avetiso.feature_statistics.databinding.ItemStatisticsWorkloadBinding
import com.avetiso.feature_statistics.models.StatisticsListItem
import com.avetiso.feature_statistics.models.TimePeriod

class StatisticsAdapter(
    private val onPeriodSelected: (TimePeriod) -> Unit,
    private val onFinanceClicked: () -> Unit,
    private val onInventorySeeAllClicked: () -> Unit,
    private val onInventoryCopyClicked: () -> Unit,
    private val onFreeSlotsClicked: () -> Unit,
    private val onSharePriceClicked: () -> Unit
) : ListAdapter<StatisticsListItem, RecyclerView.ViewHolder>(DiffCallback) {

    override fun getItemViewType(position: Int): Int {
        // Используем ID layout'ов как уникальные viewType. Никаких магических чисел.
        return when (getItem(position)) {
            is StatisticsListItem.PeriodFilter -> R.layout.item_statistics_period
            is StatisticsListItem.FinanceCard -> R.layout.item_statistics_finance
            is StatisticsListItem.InventoryWarning -> R.layout.item_statistics_inventory
            is StatisticsListItem.Workload -> R.layout.item_statistics_workload
            is StatisticsListItem.QuickActions -> R.layout.item_statistics_quick_actions
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            R.layout.item_statistics_period -> PeriodViewHolder(
                ItemStatisticsPeriodBinding.inflate(inflater, parent, false),
                onPeriodSelected
            )

            R.layout.item_statistics_finance -> FinanceViewHolder(
                ItemStatisticsFinanceBinding.inflate(inflater, parent, false),
                onFinanceClicked
            )

            R.layout.item_statistics_inventory -> InventoryViewHolder(
                ItemStatisticsInventoryBinding.inflate(inflater, parent, false),
                onInventorySeeAllClicked,
                onInventoryCopyClicked
            )

            R.layout.item_statistics_workload -> WorkloadViewHolder(
                ItemStatisticsWorkloadBinding.inflate(inflater, parent, false)
            )

            R.layout.item_statistics_quick_actions -> QuickActionsViewHolder(
                ItemStatisticsQuickActionsBinding.inflate(inflater, parent, false),
                onFreeSlotsClicked,
                onSharePriceClicked
            )

            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is PeriodViewHolder -> holder.bind(item as StatisticsListItem.PeriodFilter)
            is FinanceViewHolder -> holder.bind(item as StatisticsListItem.FinanceCard)
            is InventoryViewHolder -> holder.bind(item as StatisticsListItem.InventoryWarning)
            is WorkloadViewHolder -> holder.bind(item as StatisticsListItem.Workload)
            is QuickActionsViewHolder -> holder.bind(item as StatisticsListItem.QuickActions)
        }
    }

    // --- ViewHolders ---

    class PeriodViewHolder(
        private val binding: ItemStatisticsPeriodBinding,
        private val onPeriodSelected: (TimePeriod) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StatisticsListItem.PeriodFilter) {
            binding.chipToday.isChecked = item.selectedPeriod == TimePeriod.TODAY
            binding.chipWeek.isChecked = item.selectedPeriod == TimePeriod.WEEK
            binding.chipMonth.isChecked = item.selectedPeriod == TimePeriod.MONTH
            binding.chipCustom.isChecked = item.selectedPeriod == TimePeriod.CUSTOM

            binding.chipToday.setOnClickListener { onPeriodSelected(TimePeriod.TODAY) }
            binding.chipWeek.setOnClickListener { onPeriodSelected(TimePeriod.WEEK) }
            binding.chipMonth.setOnClickListener { onPeriodSelected(TimePeriod.MONTH) }
            binding.chipCustom.setOnClickListener { onPeriodSelected(TimePeriod.CUSTOM) }

            // Если выбран кастомный период и есть текст (например "12-18 марта"), обновляем текст чипса
            if (item.selectedPeriod == TimePeriod.CUSTOM && item.customDateRange != null) {
                binding.chipCustom.text = item.customDateRange
            } else {
                binding.chipCustom.setText(R.string.statistics_filter_custom)
            }
        }
    }

    class FinanceViewHolder(
        private val binding: ItemStatisticsFinanceBinding,
        private val onFinanceClicked: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StatisticsListItem.FinanceCard) {
            val context = binding.root.context
            binding.root.setOnClickListener { onFinanceClicked() }

            binding.tvTotalRevenue.text = item.totalRevenue
            binding.tvAverageCheck.text = context.getString(R.string.statistics_finance_average_check, item.averageCheck)
            binding.tvServicesCount.text = context.getString(R.string.statistics_finance_services_count, item.servicesCount)

            if (item.isTrendPositive) {
                binding.ivTrendArrow.setImageResource(R.drawable.ic_arrow_up)
                binding.ivTrendArrow.setColorFilter(ContextCompat.getColor(context, com.avetiso.core.R.color.green))
                binding.tvTrendPercent.text = context.getString(R.string.statistics_finance_trend_positive, item.trendPercent)
            } else {
                binding.ivTrendArrow.setImageResource(R.drawable.ic_arrow_down)
                binding.ivTrendArrow.setColorFilter(ContextCompat.getColor(context, com.avetiso.core.R.color.red))
                binding.tvTrendPercent.text = context.getString(R.string.statistics_finance_trend_negative, item.trendPercent)
            }
        }
    }

    class InventoryViewHolder(
        private val binding: ItemStatisticsInventoryBinding,
        private val onSeeAllClicked: () -> Unit,
        private val onCopyClicked: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StatisticsListItem.InventoryWarning) {
            val context = binding.root.context
            val inflater = LayoutInflater.from(context)

            binding.btnSeeAll.setOnClickListener { onSeeAllClicked() }
            binding.btnCopyList.setOnClickListener { onCopyClicked() }

            binding.llInventoryContainer.removeAllViews()

            item.items.forEach { inventoryItem ->
                val subBinding = ItemStatisticsInventorySubItemBinding.inflate(inflater, binding.llInventoryContainer, true)

                subBinding.tvItemName.text = context.getString(
                    R.string.statistics_inventory_list_item_format,
                    inventoryItem.name
                )

                subBinding.tvItemLeft.text = context.getString(
                    R.string.statistics_inventory_item_left,
                    inventoryItem.leftCount,
                    inventoryItem.measureUnit
                )
            }
        }
    }

    class WorkloadViewHolder(
        private val binding: ItemStatisticsWorkloadBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StatisticsListItem.Workload) {
            val context = binding.root.context
            binding.tvNewClients.text = item.newClientsCount.toString()
            binding.tvCancellations.text = item.cancellationsCount.toString()

            // Больше никаких "ч" в коде
            binding.tvWorkHours.text = context.getString(
                R.string.statistics_load_work_hours_value,
                item.totalWorkHours
            )
        }
    }

    class QuickActionsViewHolder(
        private val binding: ItemStatisticsQuickActionsBinding,
        private val onFreeSlotsClicked: () -> Unit,
        private val onSharePriceClicked: () -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StatisticsListItem.QuickActions) {
            binding.cardFreeSlots.setOnClickListener { onFreeSlotsClicked() }
            binding.cardSharePrice.setOnClickListener { onSharePriceClicked() }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<StatisticsListItem>() {
        override fun areItemsTheSame(oldItem: StatisticsListItem, newItem: StatisticsListItem): Boolean {
            // Если классы разные, это 100% разные элементы
            if (oldItem::class != newItem::class) return false

            return when {
                oldItem is StatisticsListItem.PeriodFilter && newItem is StatisticsListItem.PeriodFilter -> true
                oldItem is StatisticsListItem.FinanceCard && newItem is StatisticsListItem.FinanceCard -> true
                oldItem is StatisticsListItem.InventoryWarning && newItem is StatisticsListItem.InventoryWarning -> true
                oldItem is StatisticsListItem.Workload && newItem is StatisticsListItem.Workload -> true
                oldItem is StatisticsListItem.QuickActions && newItem is StatisticsListItem.QuickActions -> true
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: StatisticsListItem, newItem: StatisticsListItem): Boolean {
            return oldItem == newItem
        }
    }
}