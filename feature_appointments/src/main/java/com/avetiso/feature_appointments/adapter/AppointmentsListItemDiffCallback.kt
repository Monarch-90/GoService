package com.avetiso.feature_appointments.adapter

import androidx.recyclerview.widget.DiffUtil
import com.avetiso.feature_appointments.model.AppointmentBannerAdItem
import com.avetiso.feature_appointments.model.AppointmentContentItem
import com.avetiso.feature_appointments.model.AppointmentDateHeaderItem
import com.avetiso.feature_appointments.model.AppointmentsListItem

object AppointmentsListItemDiffCallback : DiffUtil.ItemCallback<AppointmentsListItem>() {

    override fun areItemsTheSame(
        oldItem: AppointmentsListItem,
        newItem: AppointmentsListItem
    ): Boolean {
        return when {
            oldItem is AppointmentDateHeaderItem && newItem is AppointmentDateHeaderItem -> {
                oldItem.date == newItem.date
            }

            oldItem is AppointmentContentItem && newItem is AppointmentContentItem -> {
                oldItem.appointment.id == newItem.appointment.id
            }

            oldItem is AppointmentBannerAdItem && newItem is AppointmentBannerAdItem -> true

            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: AppointmentsListItem,
        newItem: AppointmentsListItem
    ): Boolean {
        return oldItem == newItem
    }
}
