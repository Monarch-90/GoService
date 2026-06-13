package com.avetiso.feature_schedule.add_appointment.steps.step1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.avetiso.common_ui.actions.ActionsViewHolder
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.feature_schedule.R
import com.avetiso.feature_schedule.databinding.ItemAvailableServiceBinding

class AvailableServiceAdapter :
    ListAdapter<ServiceEntity, AvailableServiceAdapter.ServiceViewHolder>(DiffCallback) {

    // Добавляем nullable свойство для нашего механизма действий
    var actions: RecyclerViewActions<ServiceEntity>? = null

    // Эта логика нужна для отображения состояния чекбокса
    private var selectedServices: Set<ServiceEntity> = emptySet()

    fun updateSelection(newSelection: Set<ServiceEntity>) {
        val oldSelection = selectedServices
        selectedServices = newSelection

        // Находим только те элементы, чей статус изменился
        val changedItems = (oldSelection + newSelection)
        changedItems.forEach { service ->
            val position = currentList.indexOf(service)
            if (position != -1) {
                // И обновляем точечно только их, а не весь список
                notifyItemChanged(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ServiceViewHolder(
        ItemAvailableServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = getItem(position)
        holder.bind(service, selectedServices.contains(service))
        // Вызываем метод биндинга из нашего механизма действий
        actions?.bindViewHolderActions(holder, service)
    }

    // ViewHolder наследуется от ActionsViewHolder
    class ServiceViewHolder(
        private val binding: ItemAvailableServiceBinding,
    ) : ActionsViewHolder(binding) {

        // Получаем доступ к вложенному биндингу иконок
        private val includedBinding = binding.actionsContainer

        // Реализуем обязательные поля из ActionsViewHolder
        override val actionsContainer: View = includedBinding.root
        override val editButton: View = includedBinding.btnEdit
        override val deleteButton: View = includedBinding.btnDelete

        // Метод bind не вешает слушатели, а только отображает данные
        fun bind(service: ServiceEntity, isSelected: Boolean) {
            val context = binding.root.context

            // Название услуги
            binding.tvServiceName.text = service.name

            // Название категории
            binding.tvServiceCategory.text = service.categoryName

            // Цена
            val pricePrefix = if (service.isPriceFrom) {
                context.getString(com.avetiso.core.R.string.from_)
            } else {
                ""
            }
            val priceString = "${pricePrefix}${service.price} ${service.currency}"

            binding.tvServiceDetails.text =context.getString(
                R.string.service_details_format,
                priceString,
                service.durationMinutes
            )

            binding.viewSelectedCheck.isVisible = isSelected
        }

        // Реализуем метод, который будет скрывать/показывать чекбокс или иконки
        override fun toggleActions(show: Boolean) {
            actionsContainer.isVisible = show
            binding.llItemContainer.alpha = if (show) AppConstants.Ui.ALPHA_DIMMED else AppConstants.Ui.ALPHA_OPAQUE

            if (show) {
                binding.viewSelectedCheck.visibility = View.GONE
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ServiceEntity>() {
            override fun areItemsTheSame(a: ServiceEntity, b: ServiceEntity) = a.id == b.id
            override fun areContentsTheSame(a: ServiceEntity, b: ServiceEntity) = a == b
        }
    }
}