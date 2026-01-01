package com.avetiso.feature_schedule.add_appointment.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.avetiso.common_ui.actions.ActionsViewHolder
import com.avetiso.common_ui.actions.ISwipeableHolder
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.actions.TriggerMode
import com.avetiso.feature_schedule.ScheduleConstants
import com.avetiso.feature_schedule.add_appointment.data.Appointment
import com.avetiso.feature_schedule.add_appointment.ui.toStatusColorRes
import com.avetiso.feature_schedule.add_appointment.ui.toStatusLabelRes
import com.avetiso.feature_schedule.databinding.ItemAppointmentBinding

class AppointmentAdapter(
    private val onStatusClicked: (Appointment) -> Unit,
    private val onNoteClicked: (Appointment) -> Unit,
) : ListAdapter<Appointment, AppointmentAdapter.AppointmentViewHolder>(DiffCallback) {

    var actions: RecyclerViewActions<Appointment>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val item = getItem(position)
        // Передаем режим триггера в холдер для корректной отрисовки
        holder.bind(item, actions?.triggerMode ?: TriggerMode.LONG_PRESS, onStatusClicked, onNoteClicked)
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

        fun bind(
            appointment: Appointment,
            triggerMode: TriggerMode,
            onStatusClicked: (Appointment) -> Unit,
            onNoteClicked: (Appointment) -> Unit,
        ) {
            // При биндинге сбрасываем все состояния, которые могли остаться от переиспользования
            if (triggerMode == TriggerMode.SWIPE_REVEAL) {
                binding.contentContainer.translationX = 0f
            }

            binding.tvTime.text = appointment.time
            binding.tvClientName.text = appointment.clientName
            binding.tvServiceName.text = appointment.serviceNames
            binding.tvPrice.text = appointment.price
            binding.ivIconDiscount.isVisible = appointment.hasDiscount
            binding.tvDate.text = appointment.date

            // 1. Ставим текст статусов записи
            binding.chipStatus.setText(appointment.status.toStatusLabelRes())

            // 2. Ставим цвет статусов записи
            val colorRes = appointment.status.toStatusColorRes()
            val color = ContextCompat.getColor(binding.root.context, colorRes)
            binding.chipStatus.chipIconTint = ColorStateList.valueOf(color)

            binding.chipStatus.setOnClickListener {
                onStatusClicked(appointment)
            }

            if (appointment.note.isNotBlank()) {
                binding.ivEmptyNote.visibility = View.GONE
                binding.ivFilledNote.visibility = View.VISIBLE
            } else {
                binding.ivEmptyNote.visibility = View.VISIBLE
                binding.ivFilledNote.visibility = View.GONE
            }

            // СЛУШАТЕЛЬ НА КОНТЕЙНЕР ИКОНОК ЗАМЕТОК
            binding.noteIconContainer.setOnClickListener {
                onNoteClicked(appointment)
            }
        }

        override fun toggleActions(show: Boolean) {}
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Appointment>() {
            override fun areItemsTheSame(a: Appointment, b: Appointment) = a.id == b.id
            override fun areContentsTheSame(a: Appointment, b: Appointment) = a == b
        }
    }
}