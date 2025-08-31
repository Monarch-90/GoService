package com.avetiso.feature_schedule.add_appointment.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.common_ui.actions.ActionsViewHolder
import com.avetiso.common_ui.actions.RecyclerViewActions
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
        holder.bind(item)
        // Привязываем действия
        actions?.bindViewHolderActions(holder, item)
    }

    // ViewHolder теперь наследуется от ActionsViewHolder
    class AppointmentViewHolder(private val binding: ItemAppointmentBinding) : ActionsViewHolder(binding) {

        // Делаем ссылку на контейнер публичной, чтобы иметь к ней доступ извне
        val contentContainer: View = binding.contentContainer

        // Реализуем обязательные поля
        override val actionsContainer: View = binding.actionsContainer.root
        override val editButton: View = binding.actionsContainer.btnEdit
        override val deleteButton: View = binding.actionsContainer.btnDelete

        fun bind(appointment: Appointment) {
            contentContainer.translationX = 0f // Сбрасываем сдвиг при ре-биндинге
            binding.textTime.text = appointment.time
            binding.textServiceName.text = appointment.serviceNames
            binding.textClientName.text = appointment.clientName
            binding.textPrice.text = appointment.price
        }

        // Реализуем метод для показа/скрытия иконок
        override fun toggleActions(show: Boolean) {
            // В режиме свайпа мы не будем скрывать/показывать иконки,
            // этим будет управлять ItemTouchHelper. Но метод должен быть реализован.
            // При желании здесь можно добавить анимацию появления.
            actionsContainer.isVisible = show
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Appointment>() {
            override fun areItemsTheSame(a: Appointment, b: Appointment) = a == b
            override fun areContentsTheSame(a: Appointment, b: Appointment) = a == b
        }
    }
}