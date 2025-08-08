package com.avetiso.feature_schedule.add_appointment.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.feature_schedule.add_appointment.data.Appointment
import com.avetiso.feature_schedule.databinding.ItemAppointmentBinding
import java.time.format.DateTimeFormatter

class AppointmentAdapter : ListAdapter<Appointment, AppointmentAdapter.AppointmentViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val binding = ItemAppointmentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppointmentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AppointmentViewHolder(private val binding: ItemAppointmentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        fun bind(appointment: Appointment) {
            binding.textTime.text = appointment.time.format(timeFormatter)
            binding.textServiceName.text = appointment.serviceName
            binding.textClientName.text = appointment.clientName
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Appointment>() {
            override fun areItemsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
                return oldItem === newItem // Проверка по ссылке, для простоты
            }

            override fun areContentsTheSame(oldItem: Appointment, newItem: Appointment): Boolean {
                return oldItem == newItem
            }
        }
    }
}