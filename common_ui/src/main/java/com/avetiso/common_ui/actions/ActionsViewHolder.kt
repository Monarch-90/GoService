package com.avetiso.common_ui.actions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Абстрактный ViewHolder, который требует от наследников предоставить ссылки
 * на View-компоненты действий из их собственного, конкретного ViewBinding.
 * Это гарантирует 100% отказ от findViewById.
 */
abstract class ActionsViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    abstract val actionsContainer: View
    abstract val editButton: View
    abstract val deleteButton: View
}