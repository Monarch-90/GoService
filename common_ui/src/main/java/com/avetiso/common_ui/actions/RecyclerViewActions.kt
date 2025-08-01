package com.avetiso.common_ui.actions

import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.common_ui.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

private const val TAG = "TouchDebug"

class RecyclerViewActions<T>(
    private val fragment: Fragment,
    private val recyclerView: RecyclerView,
    private val adapter: ListAdapter<T, out ActionsViewHolder>,
    private val getItemId: (T) -> Any,
    private val getItemName: (T) -> String,
    private val onEdit: (T) -> Unit,
    private val onDelete: (T) -> Unit,
    private val onItemClick: ((T) -> Unit)? = null,
) {
    var activeItemId: Any? = null
        private set

    init {
        val touchListener = ItemActionTouchListener(
            context = recyclerView.context,
            recyclerView = recyclerView,
            onLongPress = { position -> handleLongPress(position) },
            onItemClick = { position ->
                val clickedItem =
                    adapter.currentList.getOrNull(position) ?: return@ItemActionTouchListener
                val isActionMenuOpen = activeItemId != null

                // Если меню действий открыто, любой клик его просто закрывает.
                if (isActionMenuOpen) {
                    dismissActions()
                } else {
                    // Если меню было закрыто, то это обычный клик для выбора.
                    onItemClick?.invoke(clickedItem)
                }
            },
            onEmptySpaceClick = { dismissActions() }
        )
        recyclerView.addOnItemTouchListener(touchListener)

        fragment.viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                dismissActions()
            }
        })
    }

    private fun handleLongPress(position: Int) {
        val newActiveItem = adapter.currentList.getOrNull(position) ?: return
        val newActiveId = getItemId(newActiveItem)

        val oldActiveId = activeItemId
        val oldPosition = if (oldActiveId != null) findIndexOfItem(oldActiveId) else null

        activeItemId = newActiveId

        oldPosition?.let { adapter.notifyItemChanged(it) }
        adapter.notifyItemChanged(position)
    }

    fun dismissActions() {
        val position = if (activeItemId != null) findIndexOfItem(activeItemId!!) else null
        if (position != null) {
            activeItemId = null
            adapter.notifyItemChanged(position)
        }
    }

    private fun findIndexOfItem(id: Any): Int? {
        return adapter.currentList.indexOfFirst { getItemId(it) == id }
            .takeIf { it != -1 }
    }

    fun bindViewHolderActions(holder: ActionsViewHolder, item: T) {
        val itemId = getItemId(item)
        val isActionsVisible = (itemId == activeItemId)

        holder.toggleActions(isActionsVisible)

        if (isActionsVisible) {
            holder.editButton.setOnClickListener {
                onEdit(item)
                dismissActions()
            }
            holder.deleteButton.setOnClickListener {
                showDeleteConfirmationDialog(item)
                dismissActions()
            }
        }
    }

    private fun showDeleteConfirmationDialog(item: T) {
        MaterialAlertDialogBuilder(fragment.requireContext())
            .setTitle(fragment.getString(R.string.delete_dialog_title))
            .setMessage(fragment.getString(R.string.delete_dialog_message, getItemName(item)))
            .setNegativeButton(fragment.getString(R.string.action_cancel), null)
            .setPositiveButton(fragment.getString(R.string.action_delete)) { _, _ ->
                onDelete(item)
            }
            .show()
    }
}