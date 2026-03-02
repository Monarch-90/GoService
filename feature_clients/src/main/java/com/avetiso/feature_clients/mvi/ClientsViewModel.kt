package com.avetiso.feature_clients.mvi

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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val clientDao: ClientDao,
) : ViewModel() {

    private val _state = MutableStateFlow(ClientsState())
    val state = _state.asStateFlow()

    // ✅ State: кого хотим удалить
    private var clientPendingDelete: ClientEntity? = null

    init {
        // Подписываемся на изменения поискового запроса
        _state
            .debounce(AppConstants.Time.SEARCH_DEBOUNCE)
            .flatMapLatest { state -> // flatMapLatest отменяет предыдущий запрос при новом поисковом запросе
                if (state.searchQuery.isBlank()) {
                    clientDao.getAllClients()
                } else {
                    clientDao.searchClients(state.searchQuery)
                }
            }
            .onEach { clients -> // Получает результат от flatMapLatest
                _state.update { it.copy(clients = clients) }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { it.copy(searchQuery = query) }
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