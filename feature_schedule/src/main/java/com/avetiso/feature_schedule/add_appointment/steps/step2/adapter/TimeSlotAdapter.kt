package com.avetiso.feature_schedule.add_appointment.steps.step2.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.avetiso.common_ui.actions.ActionsViewHolder
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.core.entity.TimeSlotEntity
import com.avetiso.feature_schedule.databinding.ItemTimeSlotBinding
import com.avetiso.core.R as CoreR

class TimeSlotAdapter :
    ListAdapter<TimeSlotEntity, TimeSlotAdapter.TimeSlotViewHolder>(DiffCallback) {

    private var selectedItemIds: Set<Long> = emptySet()
    var actions: RecyclerViewActions<TimeSlotEntity>? = null

    fun setSelectedItems(newItemIds: Set<Long>) {
        val oldIds = this.selectedItemIds
        this.selectedItemIds = newItemIds

        // Находим все ID, чье состояние выделения могло измениться
        val idsToUpdate = oldIds.union(newItemIds)

        idsToUpdate.forEach { id ->
            findPositionById(id)?.let { position ->
                notifyItemChanged(position)
            }
        }
    }

    private fun findPositionById(id: Long): Int? {
        val position = currentList.indexOfFirst { it.id == id }
        return if (position != -1) position else null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val binding = ItemTimeSlotBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimeSlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        val item = getItem(position)

        actions?.bindViewHolderActions(holder, item)
        // Проверяем, содержится ли ID текущего элемента в наборе выделенных
        holder.bind(item, selectedItemIds.contains(item.id))
    }

    class TimeSlotViewHolder(
        private val binding: ItemTimeSlotBinding,
    ) : ActionsViewHolder(binding) {

        override val actionsContainer: View = binding.actionsLayout.root
        override val editButton: View = binding.actionsLayout.btnEdit
        override val deleteButton: View = binding.actionsLayout.btnDelete

        override fun toggleActions(show: Boolean) {
            binding.actionsLayout.root.isVisible = show
            binding.textTime.alpha = if (show) 0.2f else 1.0f
        }

        fun bind(timeSlot: TimeSlotEntity, isSelected: Boolean) {
            val hours = timeSlot.startTimeMinutes / 60
            val minutes = timeSlot.startTimeMinutes % 60
            binding.textTime.text = String.format("%02d:%02d", hours, minutes)

            val context = binding.root.context

            if (isSelected && !binding.actionsLayout.root.isVisible) {
                binding.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, CoreR.color.custom_main)
                )
                binding.textTime.setTextColor(ContextCompat.getColor(context, CoreR.color.white))
            } else {
                binding.cardView.setCardBackgroundColor(
                    ContextCompat.getColor(context, CoreR.color.background_color)
                )
                binding.textTime.setTextColor(
                    ContextCompat.getColor(
                        context,
                        CoreR.color.custom_black_white
                    )
                )
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<TimeSlotEntity>() {
            override fun areItemsTheSame(old: TimeSlotEntity, new: TimeSlotEntity) =
                old.id == new.id

            override fun areContentsTheSame(old: TimeSlotEntity, new: TimeSlotEntity) = old == new
        }
    }
}