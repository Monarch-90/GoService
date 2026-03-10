package com.avetiso.feature_schedule.add_appointment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.actions.TriggerMode
import com.avetiso.common_ui.appointments.AppointmentViewHolder
import com.avetiso.common_ui.databinding.ItemAppointmentBinding
import com.avetiso.core.entity.ui.Appointment

class AppointmentAdapter(
    private val onStatusClicked: (Appointment) -> Unit,
    private val onNoteClicked: (Appointment) -> Unit,
) : ListAdapter<Appointment, AppointmentViewHolder>(DiffCallback) {

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

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Appointment>() {
            override fun areItemsTheSame(a: Appointment, b: Appointment) = a.id == b.id
            override fun areContentsTheSame(a: Appointment, b: Appointment) = a == b
        }
    }
}