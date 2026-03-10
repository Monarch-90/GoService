package com.avetiso.common_ui.appointments // или твой пакет в common_ui

import android.content.res.ColorStateList
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.avetiso.common_ui.actions.ActionsViewHolder
import com.avetiso.common_ui.actions.ISwipeableHolder
import com.avetiso.common_ui.actions.TriggerMode
import com.avetiso.common_ui.databinding.ItemAppointmentBinding
import com.avetiso.core.entity.ui.Appointment
import com.avetiso.core.utils.toStatusColorRes
import com.avetiso.core.utils.toStatusLabelRes

class AppointmentViewHolder(private val binding: ItemAppointmentBinding) :
    ActionsViewHolder(binding), ISwipeableHolder {

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