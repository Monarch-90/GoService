package com.avetiso.feature_schedule.add_appointment.steps.step1.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.core.entity.ServiceEntity
import com.avetiso.feature_schedule.databinding.ItemAvailableServiceBinding

class AvailableServiceAdapter(
    private val onServiceSelected: (ServiceEntity, Boolean) -> Unit,
) : ListAdapter<ServiceEntity, AvailableServiceAdapter.ServiceViewHolder>(DiffCallback) {

    private var selectedServices: Set<ServiceEntity> = emptySet()

    fun updateSelection(selected: Set<ServiceEntity>) {
        if (selectedServices != selected) {
            selectedServices = selected
            notifyDataSetChanged() // Для адаптера это самый простой способ перерисовать видимые элементы
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ServiceViewHolder(
        ItemAvailableServiceBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onServiceSelected
    )

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = getItem(position)
        holder.bind(service, selectedServices.contains(service))
    }

    class ServiceViewHolder(
        private val binding: ItemAvailableServiceBinding,
        private val onServiceSelected: (ServiceEntity, Boolean) -> Unit,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(service: ServiceEntity, isSelected: Boolean) {
            binding.textServiceName.text = service.name
            binding.textServiceDetails.text =
                "${service.price} BYN • ${service.durationMinutes} мин"
            binding.checkboxSelectService.setOnCheckedChangeListener(null) // Убираем старый лисенер
            binding.checkboxSelectService.isChecked = isSelected
            binding.checkboxSelectService.setOnCheckedChangeListener { _, checked ->
                onServiceSelected(service, checked)
            }
            binding.root.setOnClickListener {
                binding.checkboxSelectService.toggle()
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<ServiceEntity>() {
            override fun areItemsTheSame(a: ServiceEntity, b: ServiceEntity) = a.id == b.id
            override fun areContentsTheSame(a: ServiceEntity, b: ServiceEntity) = a == b
        }
    }
}