package com.avetiso.feature_clients.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    init {
        // Подписываемся на изменения поискового запроса
        _state
            .debounce(300L)
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

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            clientDao.deleteClient(client)
        }
    }
}