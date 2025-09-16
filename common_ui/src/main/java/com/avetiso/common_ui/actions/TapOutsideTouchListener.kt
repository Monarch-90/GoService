// common_ui/src/main/java/com/avetiso/common_ui/actions/TapOutsideTouchListener.kt
package com.avetiso.common_ui.actions

import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

/**
 * Умный слушатель, который реагирует на любое новое касание RecyclerView
 * для закрытия ранее открытого элемента.
 */
class TapOutsideTouchListener<T>(
    private val recyclerView: RecyclerView,
    private val adapter: ListAdapter<T, *>,
    private val getItemId: (T) -> Any,
    private val onDismiss: () -> Unit
) : RecyclerView.SimpleOnItemTouchListener() {

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        // Нас интересует только ACTION_DOWN — самое начало любого нового жеста (тап, скролл и т.д.)
        if (e.action == MotionEvent.ACTION_DOWN) {

            // Проверяем, есть ли сейчас открытый элемент (его ID хранится в теге)
            val activeItemId = rv.tag ?: return false // Если ничего не открыто, ничего не делаем

            // Если какой-то элемент открыт, любое новое касание должно его закрыть,
            // ЕСЛИ только это касание не происходит по кнопкам "Редактировать" или "Удалить".

            val activeViewHolder = findViewHolderById(activeItemId) as? ActionsViewHolder

            val touchIsInActions = if (activeViewHolder != null) {
                isTouchInView(activeViewHolder.editButton, e) || isTouchInView(activeViewHolder.deleteButton, e)
            } else {
                false
            }

            // Если тап был НЕ по кнопкам действий, даем команду на закрытие
            if (!touchIsInActions) {
                onDismiss()
            }
        }

        // ВАЖНО: всегда возвращаем false. Мы не "потребляем" событие касания,
        // а только реагируем на него. Это позволяет касанию "пройти" дальше и,
        // например, сработать клику по кнопке или начать скролл.
        return false
    }

    private fun findViewHolderById(id: Any): RecyclerView.ViewHolder? {
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(child)
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val item = adapter.currentList.getOrNull(position) ?: continue
                if (getItemId(item) == id) {
                    return holder
                }
            }
        }
        return null
    }

    private fun isTouchInView(view: View?, event: MotionEvent): Boolean {
        if (view == null || !view.isVisible) return false
        val viewRect = Rect()
        view.getGlobalVisibleRect(viewRect)
        return viewRect.contains(event.rawX.toInt(), event.rawY.toInt())
    }
}