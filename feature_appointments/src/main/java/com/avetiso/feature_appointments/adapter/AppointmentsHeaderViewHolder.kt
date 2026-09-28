package com.avetiso.feature_appointments.adapter

import androidx.recyclerview.widget.RecyclerView
import com.avetiso.feature_appointments.databinding.ItemDateHeaderBinding

class AppointmentsHeaderViewHolder(
    private val binding: ItemDateHeaderBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(dateText: String) {
        binding.tvDateHeader.text = dateText
    }
}
