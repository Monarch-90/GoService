package com.avetiso.feature_clients.selector.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.entity.ClientEntity
import com.avetiso.feature_clients.model.ClientBannerAdItem
import com.avetiso.feature_clients.model.ClientContentItem
import com.avetiso.feature_clients.model.ClientsListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ClientSelectorViewModel @Inject constructor(
    private val clientDao: ClientDao
) : ViewModel() {

    private val _state = MutableStateFlow(ClientSelectorState())
    val state = _state.asStateFlow()

    private val clients = MutableStateFlow<List<ClientEntity>>(emptyList())

    private var clientPendingDelete: ClientEntity? = null

    init {
        observeClients()
        observeListItems()
    }

    fun onSearchQueryChanged(query: String) {
        _state.update { state ->
            state.copy(searchQuery = query)
        }
    }

    fun onClientSelected(client: ClientEntity) {
        _state.update { state ->
            val newSelection = if (state.selectedClient == client) {
                null
            } else {
                client
            }
            state.copy(selectedClient = newSelection)
        }
    }

    fun clearClientSelection() {
        _state.update { state ->
            state.copy(selectedClient = null)
        }
    }

    fun onDeleteIconClicked(client: ClientEntity) {
        clientPendingDelete = client
    }

    fun onDeleteConfirmed() {
        val client = clientPendingDelete ?: return

        viewModelScope.launch {
            clientDao.deleteClient(client)
            clientPendingDelete = null
        }
    }

    private fun observeClients() {
        _state
            .map { state -> state.searchQuery }
            .distinctUntilChanged()
            .debounce(AppConstants.Time.SEARCH_DEBOUNCE)
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    clientDao.getAllClients()
                } else {
                    clientDao.searchClients(query)
                }
            }
            .onEach { clientList ->
                clients.value = clientList
            }
            .launchIn(viewModelScope)
    }

    private fun observeListItems() {
        clients
            .onEach { clientList ->
                _state.update { state ->
                    state.copy(items = buildListItems(clientList))
                }
            }
            .launchIn(viewModelScope)
    }

    private fun buildListItems(
        clientList: List<ClientEntity>
    ): List<ClientsListItem> {
        return buildList {
            add(ClientBannerAdItem)

            clientList.forEach { client ->
                add(ClientContentItem(client))
            }
        }
    }
}
