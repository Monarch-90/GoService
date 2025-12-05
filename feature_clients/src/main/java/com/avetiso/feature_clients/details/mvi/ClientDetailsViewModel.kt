package com.avetiso.feature_clients.details.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.ClientDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientDetailsViewModel @Inject constructor(
    private val clientDao: ClientDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(ClientDetailsState())
    val state = _state.asStateFlow()

    private val _events = Channel<ClientDetailsEvent>()
    val events = _events.receiveAsFlow()

    private val clientId: Long = checkNotNull(savedStateHandle["clientId"])

    init {
        observeClientData()
    }

    private fun observeClientData() {
        // Сразу показываем загрузку
        _state.update { it.copy(isLoading = true) }

        clientDao.getClientFlowById(clientId)
            .onEach { client ->
                // Как только в БД что-то изменится, этот блок сработает сам
                _state.update {
                    it.copy(
                        client = client,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEditClicked() {
        viewModelScope.launch {
            _events.send(ClientDetailsEvent.EditClient)
        }
    }

    fun onDeleteClicked() {
        val currentClient = _state.value.client ?: return
        viewModelScope.launch {
            // 1. Удаляем из БД
            clientDao.deleteClient(currentClient)
            // 2. Отправляем специфическое событие удаления
            _events.send(ClientDetailsEvent.DeleteClient)
        }
    }

    fun onBackClicked() {
        viewModelScope.launch {
            _events.send(ClientDetailsEvent.NavigateBack)
        }
    }
}