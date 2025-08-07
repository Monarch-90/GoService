package com.avetiso.feature_schedule.add_appointment.steps.step1.add_service.category

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.avetiso.common_ui.actions.ActionsViewHolder
import com.avetiso.common_ui.actions.RecyclerViewActions
import com.avetiso.core.entity.CategoryEntity
import com.avetiso.feature_schedule.databinding.ItemCategoryBinding

class CategoryAdapter :
    ListAdapter<CategoryEntity, CategoryAdapter.CategoryViewHolder>(DiffCallback) {

    var actions: RecyclerViewActions<CategoryEntity>? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = getItem(position)
        holder.bind(category)
        // Используем безопасный вызов ?.
        actions?.bindViewHolderActions(holder, category)
    }

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
    ) : ActionsViewHolder(binding) {
        override val actionsContainer: View = binding.actionsContainer.root
        override val editButton: View = binding.actionsContainer.buttonEdit
        override val deleteButton: View = binding.actionsContainer.buttonDelete

        fun bind(category: CategoryEntity) {
            binding.textCategoryName.text = category.name
        }

        override fun toggleActions(show: Boolean) {
            actionsContainer.isVisible = show
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CategoryEntity>() {
            override fun areItemsTheSame(a: CategoryEntity, b: CategoryEntity) = a.id == b.id
            override fun areContentsTheSame(a: CategoryEntity, b: CategoryEntity) = a == b
        }
    }
}