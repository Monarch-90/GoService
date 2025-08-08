package com.avetiso.common_ui.actions

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class ActionsViewHolder(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
    abstract val actionsContainer: View
    abstract val editButton: View
    abstract val deleteButton: View

    /**
     * Этот метод будет отвечать за визуальное переключение
     * между обычным состоянием и режимом действий.
     * @param show true, если нужно показать иконки действий, false - если обычное состояние.
     */
    abstract fun toggleActions(show: Boolean)
}