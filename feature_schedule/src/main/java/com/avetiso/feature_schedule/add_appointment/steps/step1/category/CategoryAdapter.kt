package com.avetiso.feature_schedule.add_appointment.steps.step1.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.core.entity.CategoryEntity
import com.avetiso.feature_schedule.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val onCategoryClick: (CategoryEntity) -> Unit
) : ListAdapter<CategoryEntity, CategoryAdapter.CategoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = CategoryViewHolder(
        ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        onCategoryClick
    )

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) = holder.bind(getItem(position))

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
        private val onCategoryClick: (CategoryEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(category: CategoryEntity) {
            binding.textCategoryName.text = category.name
            binding.root.setOnClickListener { onCategoryClick(category) }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<CategoryEntity>() {
            override fun areItemsTheSame(a: CategoryEntity, b: CategoryEntity) = a.id == b.id
            override fun areContentsTheSame(a: CategoryEntity, b: CategoryEntity) = a == b
        }
    }
}