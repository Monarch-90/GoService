package com.avetiso.feature_schedule.add_appointment.adapter

import androidx.recyclerview.widget.DiffUtil
import com.avetiso.feature_schedule.model.ScheduleAppointmentItem
import com.avetiso.feature_schedule.model.ScheduleBannerAdItem
import com.avetiso.feature_schedule.model.ScheduleListItem

object ScheduleListItemDiffCallback : DiffUtil.ItemCallback<ScheduleListItem>() {

    override fun areItemsTheSame(
        oldItem: ScheduleListItem,
        newItem: ScheduleListItem
    ): Boolean {
        return when {
            oldItem is ScheduleAppointmentItem && newItem is ScheduleAppointmentItem -> {
                oldItem.appointment.id == newItem.appointment.id
            }

            oldItem is ScheduleBannerAdItem && newItem is ScheduleBannerAdItem -> true

            else -> false
        }
    }

    override fun areContentsTheSame(
        oldItem: ScheduleListItem,
        newItem: ScheduleListItem
    ): Boolean {
        return oldItem == newItem
    }
}
