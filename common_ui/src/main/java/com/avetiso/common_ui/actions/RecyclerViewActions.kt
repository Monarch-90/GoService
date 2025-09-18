package com.avetiso.common_ui.actions

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.common_ui.R
import com.avetiso.common_ui.actions.listeners.ItemActionTouchListener
import com.avetiso.common_ui.actions.listeners.SwipeRevealTouchListener
import com.avetiso.common_ui.actions.listeners.TapOutsideTouchListener
import com.avetiso.common_ui.databinding.DeleteDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class RecyclerViewActions<T : Any>(
    private val fragment: Fragment,
    private val recyclerView: RecyclerView,
    private val adapter: ListAdapter<T, out ActionsViewHolder>,
    val getItemId: (T) -> Any,
    private val getItemName: (T) -> String,
    private val onEdit: (T) -> Unit,
    private val onDelete: (T) -> Unit,
    private val onItemClick: ((T) -> Unit)? = null,
    private val onActionsShown: () -> Unit,
    private val onActionsDismissed: () -> Unit = {},
    val triggerMode: TriggerMode = TriggerMode.LONG_PRESS,
) {
    var activeItemId: Any?
        // Используем тег RecyclerView для хранения ID активного элемента.
        // Это позволяет разным слушателям "общаться" друг с другом.
        get() = recyclerView.tag as? Long
        private set(value) {
            recyclerView.tag = value
        }

    init {
        // Полностью переработанная логика.
        // Вместо ItemTouchHelper мы добавляем наши кастомные слушатели.
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
                // ОБНОВЛЕННЫЙ ВЫЗОВ КОНСТРУКТОРА
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
                    // Эта лямбда вызывается при тапе на иконку "Редактировать"
                    onEdit = { position ->
                        adapter.currentList.getOrNull(position)?.let { item ->
                            onEdit(item)       // 1. Выполняем действие (переход на экран)
                            dismissActions()   // 2. Закрываем свайп
                        }
                    },
                    // Эта лямбда теперь правильно вызывает диалог
                    onDelete = { position ->
                        adapter.currentList.getOrNull(position)?.let { item ->
                            showDeleteConfirmationDialog(item) // 1. Показываем диалог
                            dismissActions()                   // 2. Закрываем свайп
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
        val newActiveId = getItemId(newActiveItem)
        if (newActiveId == activeItemId) return

        dismissActions()
        onActionsShown()
        activeItemId = newActiveId

        val holder = recyclerView.findViewHolderForAdapterPosition(position) ?: return

        if (triggerMode == TriggerMode.LONG_PRESS) {
            adapter.notifyItemChanged(position)
        } else { // SWIPE_REVEAL
            if (holder is ActionsViewHolder && holder is ISwipeableHolder) {
                // Анимация теперь вызывается прямо отсюда.
                holder.contentContainer.animate()
                    .translationX(-holder.actionsContainer.width.toFloat())
                    .setDuration(250)
                    .start()
            }
        }
    }

    fun dismissActions() {
        onActionsDismissed()

        val oldActiveId = activeItemId ?: return
        val oldPosition = findIndexOfItem(oldActiveId)

        activeItemId = null // Сбрасываем ID

        if (oldPosition != null) {
            val holder = recyclerView.findViewHolderForAdapterPosition(oldPosition)
            if (triggerMode == TriggerMode.LONG_PRESS) {
                adapter.notifyItemChanged(oldPosition)
            } else { // SWIPE_REVEAL
                // Анимация закрытия тоже здесь.
                (holder as? ISwipeableHolder)?.contentContainer?.animate()?.translationX(0f)?.alpha(1.0f)?.setDuration(250)
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
        val isActionsVisible = (itemId == activeItemId)

        // Для LongPress мы по-прежнему используем этот метод, чтобы показать/скрыть view
        if (triggerMode == TriggerMode.LONG_PRESS) {
            holder.toggleActions(isActionsVisible)
        }

        // Для Swipe, если элемент неактивен, принудительно ставим его в начальное положение.
        // Это важно при переиспользовании ViewHolder'ов.
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
                    showDeleteConfirmationDialog(item)
                    dismissActions()
                }
            } else {
                // Обязательно очищаем слушатели для переиспользуемых ViewHolder'ов
                holder.editButton.setOnClickListener(null)
                holder.deleteButton.setOnClickListener(null)
            }
        }
    }

    private fun showDeleteConfirmationDialog(item: T) {
        // "Надуваем" кастомный макет
        val binding = DeleteDialogBinding.inflate(LayoutInflater.from(fragment.requireContext()))

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
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_box_corners)
    }
}