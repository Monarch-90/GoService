package com.avetiso.common_ui.actions

import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.common_ui.actions.listeners.ItemActionTouchListener
import com.avetiso.common_ui.actions.listeners.SwipeRevealTouchListener
import com.avetiso.common_ui.actions.listeners.TapOutsideTouchListener

class RecyclerViewActions<T : Any>(
    private val fragment: Fragment,
    private val recyclerView: RecyclerView,
    private val adapter: ListAdapter<T, out RecyclerView.ViewHolder>,
    val getItemId: (T) -> Any,
    private val onEdit: (T) -> Unit,
    private val onDeleteClicked: (T) -> Unit,
    private val onItemClick: ((T) -> Unit)? = null,
    private val onActionsShown: (() -> Unit)? = null,
    private val onActionsDismissed: (() -> Unit)? = null,
    private val canShowActions: (T) -> Boolean = { true },
    val triggerMode: TriggerMode = TriggerMode.LONG_PRESS,
) {
    var activeItemId: Any?
        get() = recyclerView.tag
        private set(value) {
            recyclerView.tag = value
        }

    init {
        when (triggerMode) {
            TriggerMode.LONG_PRESS -> {
                val touchListener = ItemActionTouchListener(
                    context = recyclerView.context,
                    recyclerView = recyclerView,
                    onLongPress = { position -> showActionsForPosition(position) },
                    onItemClick = { position ->
                        val clickedItem = adapter.currentList.getOrNull(position)
                            ?: return@ItemActionTouchListener

                        if (activeItemId != null) {
                            dismissActions()
                        } else {
                            onItemClick?.invoke(clickedItem)
                        }
                    },
                    onEmptySpaceClick = { dismissActions() }
                )
                recyclerView.addOnItemTouchListener(touchListener)
            }

            TriggerMode.SWIPE_REVEAL -> {
                val swipeListener = SwipeRevealTouchListener(
                    recyclerView = recyclerView,
                    adapter = adapter,
                    getItemId = getItemId,
                    onActionsRevealed = { position -> showActionsForPosition(position) },
                    onDismiss = { dismissActions() }
                )

                val tapListener = TapOutsideTouchListener(
                    recyclerView = recyclerView,
                    adapter = adapter,
                    getItemId = getItemId,
                    onDismiss = { dismissActions() },
                    onEdit = { position ->
                        adapter.currentList.getOrNull(position)?.let { item ->
                            onEdit(item)
                            dismissActions()
                        }
                    },
                    onDelete = { position ->
                        adapter.currentList.getOrNull(position)?.let { item ->
                            onDeleteClicked(item)
                            dismissActions()
                        }
                    }
                )

                recyclerView.addOnItemTouchListener(swipeListener)
                recyclerView.addOnItemTouchListener(tapListener)
            }
        }

        fragment.viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                dismissActions()
            }
        })
    }

    fun showActionsForPosition(position: Int) {
        val newActiveItem = adapter.currentList.getOrNull(position) ?: return

        if (!canShowActions(newActiveItem)) {
            return
        }

        val newActiveId = getItemId(newActiveItem)
        if (newActiveId == activeItemId) {
            return
        }

        dismissActions()
        onActionsShown?.invoke()
        activeItemId = newActiveId

        val holder = recyclerView.findViewHolderForAdapterPosition(position) ?: return

        if (triggerMode == TriggerMode.LONG_PRESS) {
            adapter.notifyItemChanged(position)
        } else if (holder is ActionsViewHolder && holder is ISwipeableHolder) {
            holder.contentContainer.animate()
                .translationX(-holder.actionsContainer.width.toFloat())
                .setDuration(250)
                .start()
        }
    }

    fun dismissActions() {
        onActionsDismissed?.invoke()

        val oldActiveId = activeItemId ?: return
        val oldPosition = findIndexOfItem(oldActiveId)

        activeItemId = null

        if (oldPosition != null) {
            val holder = recyclerView.findViewHolderForAdapterPosition(oldPosition)
            if (triggerMode == TriggerMode.LONG_PRESS) {
                adapter.notifyItemChanged(oldPosition)
            } else {
                (holder as? ISwipeableHolder)
                    ?.contentContainer
                    ?.animate()
                    ?.translationX(0f)
                    ?.alpha(1.0f)
                    ?.setDuration(250)
                    ?.start()
            }
        }
    }

    private fun findIndexOfItem(id: Any): Int? {
        return adapter.currentList.indexOfFirst { getItemId(it) == id }
            .takeIf { it != -1 }
    }

    fun bindViewHolderActions(holder: ActionsViewHolder, item: T) {
        val itemId = getItemId(item)
        val isActionsVisible = itemId == activeItemId

        if (triggerMode == TriggerMode.LONG_PRESS) {
            holder.toggleActions(isActionsVisible)
        }

        if (triggerMode == TriggerMode.SWIPE_REVEAL && !isActionsVisible) {
            (holder as? ISwipeableHolder)?.contentContainer?.translationX = 0f
        }

        if (triggerMode == TriggerMode.LONG_PRESS) {
            if (isActionsVisible) {
                holder.editButton.setOnClickListener {
                    onEdit(item)
                    dismissActions()
                }
                holder.deleteButton.setOnClickListener {
                    onDeleteClicked(item)
                    dismissActions()
                }
            } else {
                holder.editButton.setOnClickListener(null)
                holder.deleteButton.setOnClickListener(null)
            }
        }
    }
}
