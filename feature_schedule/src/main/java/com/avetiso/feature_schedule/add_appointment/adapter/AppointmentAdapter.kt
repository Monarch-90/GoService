package com.avetiso.feature_schedule.add_appointment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.common_ui.actions.ActionsViewHolder
import com.avetiso.common_ui.actions.ISwipeableHolder
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.actions.TriggerMode
import com.avetiso.feature_schedule.add_appointment.data.Appointment
import com.avetiso.feature_schedule.databinding.ItemAppointmentBinding
import java.time.format.DateTimeFormatter

class AppointmentAdapter : ListAdapter<Appointment, AppointmentAdapter.AppointmentViewHolder>(DiffCallback) {

    var actions: RecyclerViewActions<Appointment>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val item = getItem(position)
        // Передаем режим триггера в холдер для корректной отрисовки
        holder.bind(item, actions?.triggerMode ?: TriggerMode.LONG_PRESS)
        actions?.bindViewHolderActions(holder, item)
    }

    // ViewHolder теперь реализует ISwipeableHolder
    class AppointmentViewHolder(private val binding: ItemAppointmentBinding) :
        ActionsViewHolder(binding), ISwipeableHolder {

        // Реализуем контракты интерфейса
        override val contentContainer: View = binding.contentContainer
        override val actionsContainer: View = binding.actionsContainer.root
        override val editButton: View = binding.actionsContainer.btnEdit
        override val deleteButton: View = binding.actionsContainer.btnDelete

        fun bind(appointment: Appointment, triggerMode: TriggerMode) {
            // При биндинге сбрасываем все состояния, которые могли остаться от переиспользования
            if (triggerMode == TriggerMode.SWIPE_REVEAL) {
                binding.contentContainer.translationX = 0f
            }
            binding.contentContainer.alpha = 1.0f

            binding.textTime.text = appointment.time
            binding.textServiceName.text = appointment.serviceNames
            binding.textClientName.text = appointment.clientName
            binding.textPrice.text = appointment.price
        }

        override fun toggleActions(show: Boolean) {
            // Эта логика затемнения идеальна для LONG_PRESS. Оставляем ее.
            binding.contentContainer.alpha = if (show) 0.5f else 1.0f
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Appointment>() {
            override fun areItemsTheSame(a: Appointment, b: Appointment) = a == b
            override fun areContentsTheSame(a: Appointment, b: Appointment) = a == b
        }
    }
}