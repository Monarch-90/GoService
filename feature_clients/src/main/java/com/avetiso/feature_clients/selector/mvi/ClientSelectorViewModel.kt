package com.avetiso.feature_clients.selector.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.entity.ClientEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientSelectorViewModel @Inject constructor(
    private val clientDao: ClientDao,
) : ViewModel() {

    private val _state = MutableStateFlow(ClientSelectorState())
    val state = _state.asStateFlow()

    // ✅ State: кого хотим удалить
    private var clientPendingDelete: ClientEntity? = null

    // Этот Flow будет переизлучать список клиентов при изменении searchQuery
    val clients = _state
        .debounce(AppConstants.Time.SEARCH_DEBOUNCE)
        .flatMapLatest { state ->
            if (state.searchQuery.isBlank()) {
                clientDao.getAllClients()
            } else {
                clientDao.searchClients(state.searchQuery)
            }
        }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    fun onClientSelected(client: ClientEntity) {
        _state.update { currentState ->
            val newSelection = if (currentState.selectedClient == client) null else client
            currentState.copy(selectedClient = newSelection)
        }
    }

    fun clearClientSelection() {
        _state.update { it.copy(selectedClient = null) }
    }

    // 1. Фрагмент говорит: нажали иконку удаления
    fun onDeleteIconClicked(client: ClientEntity) {
        clientPendingDelete = client
    }

    // 2. Фрагмент говорит: пользователь нажал "Да"
    fun onDeleteConfirmed() {
        val client = clientPendingDelete ?: return
        viewModelScope.launch {
            clientDao.deleteClient(client)
        }
        clientPendingDelete = null
    }
}