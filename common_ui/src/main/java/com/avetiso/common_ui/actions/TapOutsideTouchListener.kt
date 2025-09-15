package com.avetiso.common_ui.actions

import android.content.Context
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class TapOutsideTouchListener<T>(
    context: Context,
    private val recyclerView: RecyclerView,
    private val adapter: ListAdapter<T, *>,
    private val getItemId: (T) -> Any,
    private val onDismiss: () -> Unit,
) : RecyclerView.SimpleOnItemTouchListener() {

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val activeItemId = recyclerView.tag ?: return false
            val activeViewHolder = findViewHolderById(activeItemId) as? ActionsViewHolder

            if (activeViewHolder != null) {
                val wasActionClicked = isTouchInView(activeViewHolder.editButton, e) ||
                        isTouchInView(activeViewHolder.deleteButton, e)
                if (!wasActionClicked) {
                    onDismiss()
                }
            } else {
                onDismiss()
            }
            return false
        }
    })

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        if (rv.tag != null) {
            gestureDetector.onTouchEvent(e)
        }
        return false
    }

    private fun findViewHolderById(id: Any): RecyclerView.ViewHolder? {
        for (i in 0 until recyclerView.childCount) {
            val child = recyclerView.getChildAt(i)
            val holder = recyclerView.getChildViewHolder(child)
            val position = holder.bindingAdapterPosition
            if (position != RecyclerView.NO_POSITION) {
                // Получаем элемент из адаптера по позиции
                val item = adapter.currentList[position]
                // Используем переданную лямбду, чтобы получить ID элемента
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