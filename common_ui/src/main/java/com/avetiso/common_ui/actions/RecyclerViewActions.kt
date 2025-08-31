package com.avetiso.common_ui.actions

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.common_ui.R
import com.avetiso.common_ui.databinding.CustomDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

enum class TriggerMode {
    LONG_PRESS,
    SWIPE
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
    triggerMode: TriggerMode = TriggerMode.LONG_PRESS,
) {
    var activeItemId: Any? = null
        private set

    init {
        // 3. Устанавливаем слушатель только для режима LONG_PRESS
        if (triggerMode == TriggerMode.LONG_PRESS) {
            val touchListener = ItemActionTouchListener(
                context = recyclerView.context,
                recyclerView = recyclerView,
                onLongPress = { position -> showActionsForPosition(position) }, // Используем новый публичный метод
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

        fragment.viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onPause(owner: LifecycleOwner) {
                dismissActions()
            }
        })
    }

    fun showActionsForPosition(position: Int) {
        // Если кликнули по уже активному элементу, ничего не делаем
        val newActiveItem = adapter.currentList.getOrNull(position) ?: return
        if (getItemId(newActiveItem) == activeItemId) return

        onActionsShown()

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