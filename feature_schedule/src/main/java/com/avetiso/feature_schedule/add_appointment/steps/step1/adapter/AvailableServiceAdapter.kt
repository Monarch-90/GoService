package com.avetiso.feature_schedule.add_appointment.steps.step1.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.avetiso.common_ui.actions.ActionsViewHolder
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.core.entity.ServiceEntity
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
        override val editButton: View = includedBinding.buttonEdit
        override val deleteButton: View = includedBinding.buttonDelete

        // Метод bind не вешает слушатели, а только отображает данные
        fun bind(service: ServiceEntity, isSelected: Boolean) {

            // Название услуги
            binding.textServiceName.text = service.name

            // Название категории
            binding.textServiceCategory.text = service.categoryName

            // Цена
            val pricePrefix = if (service.isPriceFrom) "от " else ""
            val priceString = "${pricePrefix}${service.price} ${service.currency}"

            // продолжительность в формате ч:мм
//            val durationInMinutes = service.durationMinutes
//            val hours = durationInMinutes / 60
//            val minutes = durationInMinutes % 60
//            val durationString = when {
//                hours > 0 && minutes > 0 -> "$hours ч $minutes мин"
//                hours > 0 -> "$hours ч"
//                else -> "$minutes мин"
//            }
//
//            binding.textServiceDetails.text =
//                "$priceString • $durationString"

            binding.textServiceDetails.text =
                "$priceString • ${service.durationMinutes} мин"

            binding.imageViewSelectedCheck.isVisible = isSelected
        }

        // Реализуем метод, который будет скрывать/показывать чекбокс или иконки
        override fun toggleActions(show: Boolean) {
            actionsContainer.isVisible = show

            if (show) {
                binding.imageViewSelectedCheck.visibility = View.GONE
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