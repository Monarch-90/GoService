package com.avetiso.common_ui.actions.listeners

import android.animation.ObjectAnimator
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.avetiso.common_ui.actions.ActionsViewHolder
import com.avetiso.common_ui.actions.ISwipeableHolder
import com.avetiso.core.AppConstants
import kotlin.math.abs

class SwipeRevealTouchListener<T>(
    private val recyclerView: RecyclerView,
    private val adapter: ListAdapter<T, *>,      // <- Принимаем универсальный ListAdapter
    private val getItemId: (T) -> Any,           // <- Принимаем лямбду для получения ID
    private val onActionsRevealed: (Int) -> Unit,
    private val onDismiss: () -> Unit,
) : RecyclerView.OnItemTouchListener {

    private val touchSlop = ViewConfiguration.get(recyclerView.context).scaledTouchSlop

    private var initialX = 0f
    private var initialY = 0f
    private var isSwiping = false
    private var swipedViewHolder: RecyclerView.ViewHolder? = null

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                initialX = e.x
                initialY = e.y
                val child = rv.findChildViewUnder(e.x, e.y)
                swipedViewHolder = child?.let { rv.getChildViewHolder(it) }
                return false
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = e.x - initialX
                val dy = e.y - initialY

                if (abs(dx) > touchSlop && abs(dx) > abs(dy)) {
                    if (dx < 0 && swipedViewHolder is ISwipeableHolder) {

                        val activeItemId = rv.tag
                        var currentItemId: Any? = null

                        val position = swipedViewHolder!!.bindingAdapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val item = adapter.currentList.getOrNull(position)
                            if (item != null) {
                                currentItemId = getItemId(item)
                            }
                        }

                        if (activeItemId != null && activeItemId != currentItemId) {
                            onDismiss()
                        }

                        isSwiping = true
                        return true
                    }
                }
            }
        }
        return false
    }


    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
        val holder = swipedViewHolder
        if (holder !is ActionsViewHolder || holder !is ISwipeableHolder) {
            // Если холдер не реализует нужные интерфейсы, ничего не делаем.
            return
        }
        // Теперь компилятор "знает", что у holder есть все необходимые свойства.
        // Все предупреждения и ошибки исчезнут.

        when (e.actionMasked) {
            MotionEvent.ACTION_MOVE -> {
                val dx = e.x - initialX
                val clampedDx = dx.coerceIn(-holder.actionsContainer.width.toFloat(), 0f)
                holder.contentContainer.translationX = clampedDx

                // Рассчитываем и применяем прозрачность в реальном времени
                val actionsWidth = holder.actionsContainer.width.toFloat()
                if (actionsWidth > 0) {
                    // Вычисляем прогресс свайпа (от 0.0 до 1.0)
                    val swipeProgress = abs(clampedDx) / actionsWidth

                    // Целевая прозрачность - 0.5f. Диапазон изменения - 0.8f (от 1.0 до 0.5)
                    val alphaRange = 1.0f - 0.5f

                    // Вычисляем новую прозрачность
                    val newAlpha = 1.0f - (swipeProgress * alphaRange)

                    // Применяем ее к контейнеру
                    holder.contentContainer.alpha = newAlpha
                }
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val swipedDistance = abs(holder.contentContainer.translationX)
                val position = holder.bindingAdapterPosition

                if (swipedDistance > 0 && position != RecyclerView.NO_POSITION) {
                    onActionsRevealed(position)
                } else {
                    animateSwipe(holder.contentContainer, 0f)
                }

                resetState()
            }
        }
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}

    private fun resetState() {
        isSwiping = false
        swipedViewHolder = null
        initialX = 0f
        initialY = 0f
    }

    private fun animateSwipe(view: View, targetX: Float) {
        ObjectAnimator.ofFloat(view, View.TRANSLATION_X, targetX)
            .setDuration(AppConstants.Time.SWIPE_REMOVE)
            .start()
    }
}