package com.avetiso.common_ui.actions

import android.content.Context
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView

class ItemActionTouchListener(
    context: Context,
    private val recyclerView: RecyclerView,
    private val onLongPress: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit,
    private val onEmptySpaceClick: () -> Unit,
) : RecyclerView.SimpleOnItemTouchListener() {

    private val gestureDetector: GestureDetector

    init {
        gestureDetector =
            GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

                override fun onLongPress(e: MotionEvent) {
                    val child = findChildUnder(e) ?: return
                    val position = recyclerView.getChildAdapterPosition(child)
                    if (position != RecyclerView.NO_POSITION) {
                        onLongPress(position)
                    }
                }

                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    val child = findChildUnder(e)
                    // Если кликнули по элементу
                    if (child != null) {
                        val viewHolder =
                            recyclerView.getChildViewHolder(child) as? ActionsViewHolder
                        // Проверяем, был ли клик по одной из видимых кнопок
                        if (viewHolder != null && (isTouchInView(
                                viewHolder.editButton,
                                e
                            ) || isTouchInView(viewHolder.deleteButton, e))
                        ) {
                            // Если да, то ничего не делаем, даем сработать OnClickListener'у самой кнопки
                            return false
                        }
                        // Иначе это обычный клик по элементу
                        val position = recyclerView.getChildAdapterPosition(child)
                        if (position != RecyclerView.NO_POSITION) {
                            onItemClick(position)
                        }
                    } else {
                        // Клик по пустому месту
                        onEmptySpaceClick()
                    }
                    return false
                }
            })
    }

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        // Просто передаем все события в GestureDetector
        gestureDetector.onTouchEvent(e)
        return false
    }

    private fun findChildUnder(e: MotionEvent): View? {
        return recyclerView.findChildViewUnder(e.x, e.y)
    }

    private fun isTouchInView(view: View?, event: MotionEvent): Boolean {
        if (view == null || !view.isVisible) return false
        val viewRect = Rect()
        view.getGlobalVisibleRect(viewRect)
        return viewRect.contains(event.rawX.toInt(), event.rawY.toInt())
    }
}