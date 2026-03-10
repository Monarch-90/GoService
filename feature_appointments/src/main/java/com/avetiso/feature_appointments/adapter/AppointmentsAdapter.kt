package com.avetiso.feature_appointments.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.actions.TriggerMode
import com.avetiso.common_ui.appointments.AppointmentViewHolder
import com.avetiso.common_ui.databinding.ItemAppointmentBinding
import com.avetiso.core.entity.ui.Appointment
import com.avetiso.feature_appointments.AppointmentsConstants
import com.avetiso.feature_appointments.model.AppointmentsListItem
import com.avetiso.feature_appointments.databinding.ItemDateHeaderBinding

class AppointmentsAdapter(
    private val onStatusClicked: (Appointment) -> Unit,
    private val onNoteClicked: (Appointment) -> Unit
) : ListAdapter<AppointmentsListItem, RecyclerView.ViewHolder>(DiffCallback) {

    // Делегат для поддержки свайпов и кнопок (удаление/редактирование)
    var actions: RecyclerViewActions<AppointmentsListItem>? = null

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is AppointmentsListItem.DateHeader -> AppointmentsConstants.ViewType.VIEW_TYPE_HEADER
            is AppointmentsListItem.AppointmentItem -> AppointmentsConstants.ViewType.VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            AppointmentsConstants.ViewType.VIEW_TYPE_HEADER -> {
                val binding = ItemDateHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }

            AppointmentsConstants.ViewType.VIEW_TYPE_ITEM -> {
                // Биндинг тянется из модуля common_ui!
                val binding = ItemAppointmentBinding.inflate(inflater, parent, false)
                AppointmentViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> {
                val header = item as AppointmentsListItem.DateHeader
                holder.bind(header.date)
            }

            is AppointmentViewHolder -> {
                val appointmentItem = item as AppointmentsListItem.AppointmentItem
                val appointment = appointmentItem.appointment

                // Настраиваем холдер в точности как в расписании
                holder.bind(
                    appointment = appointment,
                    triggerMode = actions?.triggerMode ?: TriggerMode.SWIPE_REVEAL,
                    onStatusClicked = onStatusClicked,
                    onNoteClicked = onNoteClicked
                )
                // Привязываем логику экшенов (свайпов) к конкретной записи
                actions?.bindViewHolderActions(holder, appointmentItem)
            }
        }
    }

    class HeaderViewHolder(
        private val binding: ItemDateHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dateText: String) {
            binding.tvDateHeader.text = dateText
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<AppointmentsListItem>() {
            override fun areItemsTheSame(oldItem: AppointmentsListItem, newItem: AppointmentsListItem): Boolean {
                return when (oldItem) {
                    is AppointmentsListItem.DateHeader if newItem is AppointmentsListItem.DateHeader -> {
                        oldItem.date == newItem.date
                    }

                    is AppointmentsListItem.AppointmentItem if newItem is AppointmentsListItem.AppointmentItem -> {
                        oldItem.appointment.id == newItem.appointment.id
                    }

                    else -> false
                }
            }

            override fun areContentsTheSame(oldItem: AppointmentsListItem, newItem: AppointmentsListItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}