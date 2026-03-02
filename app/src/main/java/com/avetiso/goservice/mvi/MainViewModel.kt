package com.avetiso.goservice.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Гарантированная доставка событий (навигация) без потерь при повороте экрана
    private val _effects = Channel<MainSideEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    // Сохранение таргета, которое переживет Process Death
    private var pendingDestinationId: Int?
        get() = savedStateHandle[AppConstants.Time.KEY_PENDING_DESTINATION]
        set(value) {
            savedStateHandle[AppConstants.Time.KEY_PENDING_DESTINATION] = value
        }

    // Защита от race conditions и спама кликами (Thread-safety)
    private val actionMutex = Mutex()

    fun processIntent(intent: MainIntent) {
        viewModelScope.launch {
            // Гарантируем последовательную обработку намерений
            actionMutex.withLock {
                when (intent) {
                    is MainIntent.OnSidebarItemClicked -> handleItemClick(intent.itemId)
                    is MainIntent.OnDrawerClosed -> handleDrawerClosed()
                }
            }
        }
    }

    private suspend fun handleItemClick(itemId: Int) {
        // Если навигация уже запущена (пользователь спамит), игнорируем
        if (pendingDestinationId != null) return

        pendingDestinationId = itemId
        _effects.send(MainSideEffect.CloseSidebar)
    }

    private suspend fun handleDrawerClosed() {
        val destinationId = pendingDestinationId ?: return

        // Очищаем стейт ПЕРЕД навигацией
        pendingDestinationId = null
        _effects.send(MainSideEffect.Navigate(destinationId))
    }

    override fun onCleared() {
        _effects.close()
        super.onCleared()
    }
}