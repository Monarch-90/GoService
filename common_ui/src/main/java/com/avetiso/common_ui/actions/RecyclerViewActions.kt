package com.avetiso.common_ui.actions

import android.animation.ObjectAnimator
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.common_ui.R
import com.avetiso.common_ui.databinding.CustomDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.abs

enum class TriggerMode {
    LONG_PRESS,
    SWIPE_REVEAL
}

class RecyclerViewActions<T>(
    private val fragment: Fragment,
    private val recyclerView: RecyclerView,
    private val adapter: ListAdapter<T, out ActionsViewHolder>,
    val getItemId: (T) -> Any,
    private val getItemName: (T) -> String,
    private val onEdit: (T) -> Unit,
    private val onDelete: (T) -> Unit,
    private val onItemClick: ((T) -> Unit)? = null,
    private val onActionsShown: () -> Unit,
    val triggerMode: TriggerMode = TriggerMode.LONG_PRESS,
) {
    var activeItemId: Any? = null
        private set

    init {
        when (triggerMode) {
            TriggerMode.LONG_PRESS -> {
                val touchListener = ItemActionTouchListener(
                    context = recyclerView.context,
                    recyclerView = recyclerView,
                    onLongPress = { position -> showActionsForPosition(position) },
                    onItemClick = { position ->
                        val clickedItem = adapter.currentList.getOrNull(position) ?: return@ItemActionTouchListener
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
                val swipeCallback = SwipeRevealCallback()
                ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
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
        if (getItemId(newActiveItem) == activeItemId) return

        dismissActions()
        onActionsShown()
        activeItemId = getItemId(newActiveItem)

        val holder = recyclerView.findViewHolderForAdapterPosition(position) ?: return

        if (triggerMode == TriggerMode.LONG_PRESS) {
            adapter.notifyItemChanged(position)
        } else { // SWIPE_REVEAL
            // Проверяем, что ViewHolder поддерживает оба нужных нам контракта
            if (holder is ActionsViewHolder && holder is ISwipeableHolder) {
                // Теперь у holder есть доступ и к contentContainer, и к actionsContainer
                animateSwipe(holder.contentContainer, -holder.actionsContainer.width.toFloat())
            }
        }
    }

    fun dismissActions() {
        val oldPosition = if (activeItemId != null) findIndexOfItem(activeItemId!!) else null
        if (oldPosition != null) {
            val oldActiveId = activeItemId
            activeItemId = null
            val holder = recyclerView.findViewHolderForAdapterPosition(oldPosition)
            if (triggerMode == TriggerMode.LONG_PRESS) {
                adapter.notifyItemChanged(oldPosition)
            } else { // SWIPE_REVEAL
                (holder as? ISwipeableHolder)?.let {
                    animateSwipe(it.contentContainer, 0f)
                }
            }
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

        if (triggerMode == TriggerMode.SWIPE_REVEAL && !isActionsVisible) {
            (holder as? ISwipeableHolder)?.contentContainer?.translationX = 0f
        }

        if (isActionsVisible) {
            holder.editButton.setOnClickListener { onEdit(item); dismissActions() }
            holder.deleteButton.setOnClickListener { showDeleteConfirmationDialog(item); dismissActions() }
        }
    }

    private fun animateSwipe(view: View, targetX: Float) =
        ObjectAnimator.ofFloat(view, "translationX", targetX).setDuration(250).start()

    private inner class SwipeRevealCallback : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(r: RecyclerView, v: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
        override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder) = Float.MAX_VALUE

        override fun onChildDraw(c: Canvas, r: RecyclerView, vh: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
            // ИСПРАВЛЕНИЕ ЗДЕСЬ:
            // 1. Приводим тип к ActionsViewHolder, чтобы получить доступ к actionsContainer
            val holder = vh as ActionsViewHolder
            // 2. Проверяем, реализует ли он ISwipeableHolder, чтобы получить contentContainer
            if (holder is ISwipeableHolder) {
                val clampedDx = dX.coerceIn(-holder.actionsContainer.width.toFloat(), 0f)
                holder.contentContainer.translationX = clampedDx
            }
        }

        override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
            // ИСПРАВЛЕНИЕ ЗДЕСЬ:
            val position = viewHolder.bindingAdapterPosition // Используем оригинальный viewHolder
            if (position == RecyclerView.NO_POSITION) return

            // 1. Приводим тип к ActionsViewHolder, чтобы получить доступ к actionsContainer
            val holder = viewHolder as ActionsViewHolder
            // 2. Проверяем, реализует ли он ISwipeableHolder, чтобы получить contentContainer
            if (holder is ISwipeableHolder) {
                val actionsWidth = holder.actionsContainer.width.toFloat()
                val swipedDistance = abs(holder.contentContainer.translationX)

                if (swipedDistance > actionsWidth * 0.4) {
                    showActionsForPosition(position)
                } else {
                    dismissActions()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog(item: T) {
        // "Надуваем" кастомный макет
        val binding = CustomDialogBinding.inflate(LayoutInflater.from(fragment.requireContext()))

        // Текст из string
        binding.tvMessage.text =
            fragment.getString(R.string.delete_dialog_message, getItemName(item))

        // Создаем диалог, передавая ему готовый макет
        val dialog = MaterialAlertDialogBuilder(fragment.requireContext())
            .setView(binding.root)
            .create()

        // Назначаем слушателей нажатий на наши кнопки
        binding.btnNegative.setOnClickListener {
            dialog.dismiss() // Просто закрываем диалог
        }
        binding.btnPositive.setOnClickListener {
            onDelete(item)   // Выполняем действие
            dialog.dismiss() // Или закрываем диалог
        }

        // Показываем диалог
        dialog.show()

        // Скругление фона
        dialog.window?.setBackgroundDrawableResource(com.avetiso.core.R.drawable.corners_window)
    }
}