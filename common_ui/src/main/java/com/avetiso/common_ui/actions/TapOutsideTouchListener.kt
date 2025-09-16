// common_ui/src/main/java/com/avetiso/common_ui/actions/TapOutsideTouchListener.kt
package com.avetiso.common_ui.actions

import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.view.GestureDetector
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
    private val onDismiss: () -> Unit,
    private val onEdit: (Int) -> Unit,
    private val onDelete: (Int) -> Unit,
) : RecyclerView.SimpleOnItemTouchListener() {

    private val gestureDetector = GestureDetector(recyclerView.context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val activeItemId = recyclerView.tag ?: return false
            val activeViewHolder = findViewHolderById(activeItemId) as? ActionsViewHolder
            val position = (activeViewHolder as? RecyclerView.ViewHolder)?.bindingAdapterPosition

            if (activeViewHolder != null && position != null && position != RecyclerView.NO_POSITION) {
                // Проверяем, был ли тап по кнопке редактирования
                if (isTouchInView(activeViewHolder.editButton, e)) {
                    Log.d("ACTION_DEBUG", "Нажата кнопка РЕДАКТИРОВАТЬ. Вызываем onEdit.")
                    onEdit(position)
                    return true // Событие обработано
                }
                // Проверяем, был ли тап по кнопке удаления
                if (isTouchInView(activeViewHolder.deleteButton, e)) {
                    Log.d("ACTION_DEBUG", "Нажата кнопка УДАЛИТЬ. Вызываем onDelete.")
                    onDelete(position)
                    return true // Событие обработано
                }
            }

            // Если тап был не по кнопкам, вызываем закрытие
            Log.d("ACTION_DEBUG", "Тап мимо кнопок. Вызываем onDismiss.")
            onDismiss()
            return true // Событие обработано
        }
    })

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        // Если какой-то элемент открыт, передаем все события в GestureDetector,
        // чтобы он нашел наш одиночный тап.
        if (rv.tag != null) {
            return gestureDetector.onTouchEvent(e)
        }
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