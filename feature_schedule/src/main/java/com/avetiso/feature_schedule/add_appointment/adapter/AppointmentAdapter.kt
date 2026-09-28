package com.avetiso.feature_schedule.add_appointment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.common_ui.actions.TriggerMode
import com.avetiso.common_ui.ads.BannerAdViewHolder
import com.avetiso.common_ui.appointments.AppointmentViewHolder
import com.avetiso.common_ui.databinding.ItemAppointmentBannerAdHostBinding
import com.avetiso.common_ui.databinding.ItemAppointmentBinding
import com.avetiso.core.entity.ui.Appointment
import com.avetiso.feature_schedule.ScheduleConstants
import com.avetiso.feature_schedule.model.ScheduleAppointmentItem
import com.avetiso.feature_schedule.model.ScheduleBannerAdItem
import com.avetiso.feature_schedule.model.ScheduleListItem

class AppointmentAdapter(
    private val onBannerBound: (ViewGroup) -> Unit,
    private val onBannerRecycled: (ViewGroup) -> Unit,
    private val onStatusClicked: (Appointment) -> Unit,
    private val onNoteClicked: (Appointment) -> Unit
) : ListAdapter<ScheduleListItem, RecyclerView.ViewHolder>(ScheduleListItemDiffCallback) {

    var actions: RecyclerViewActions<ScheduleListItem>? = null

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ScheduleAppointmentItem -> ScheduleConstants.ViewType.APPOINTMENT
            is ScheduleBannerAdItem -> ScheduleConstants.ViewType.BANNER_AD
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            ScheduleConstants.ViewType.APPOINTMENT -> {
                AppointmentViewHolder(
                    ItemAppointmentBinding.inflate(inflater, parent, false)
                )
            }

            ScheduleConstants.ViewType.BANNER_AD -> {
                BannerAdViewHolder(
                    container = ItemAppointmentBannerAdHostBinding
                        .inflate(inflater, parent, false)
                        .root,
                    onBannerBound = onBannerBound,
                    onBannerRecycled = onBannerRecycled
                )
            }

            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = getItem(position)) {
            is ScheduleAppointmentItem -> {
                val appointmentHolder = holder as AppointmentViewHolder
                appointmentHolder.bind(
                    appointment = item.appointment,
                    triggerMode = actions?.triggerMode ?: TriggerMode.SWIPE_REVEAL,
                    onStatusClicked = onStatusClicked,
                    onNoteClicked = onNoteClicked
                )
                actions?.bindViewHolderActions(appointmentHolder, item)
            }

            is ScheduleBannerAdItem -> {
                (holder as BannerAdViewHolder).bind()
            }
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is BannerAdViewHolder) {
            holder.recycle()
        }
        super.onViewRecycled(holder)
    }
}
