package com.avetiso.feature_clients.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.entity.ClientEntity
import com.avetiso.feature_clients.ClientsConstants
import com.avetiso.feature_clients.model.ClientBannerAdItem
import com.avetiso.feature_clients.model.ClientContentItem
import com.avetiso.feature_clients.model.ClientsListItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class ClientsViewModel @Inject constructor(
    private val clientDao: ClientDao,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ClientsState())
    val state = _state.asStateFlow()

    private val _events = Channel<ClientsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val clients = MutableStateFlow<List<ClientEntity>>(emptyList())

    private var pendingDeleteClientId: Long?
        get() = savedStateHandle[ClientsConstants.Pending.KEY_PENDING_DELETE_ID]
        set(value) {
            savedStateHandle[ClientsConstants.Pending.KEY_PENDING_DELETE_ID] = value
        }

    init {
        observeClients()
        observeListItems()
    }

    fun processIntent(intent: ClientsIntent) {
        when (intent) {
            is UpdateClientsSearchQueryIntent -> {
                _state.update { state ->
                    state.copy(searchQuery = intent.query)
                }
            }

            is OpenClientIntent -> {
                viewModelScope.launch {
                    _events.send(NavigateToClientDetailsEvent(intent.clientId))
                }
            }

            is EditClientIntent -> {
                viewModelScope.launch {
                    _events.send(NavigateToAddEditClientEvent(intent.clientId))
                }
            }

            is DeleteClientClickedIntent -> {
                pendingDeleteClientId = intent.clientId
                viewModelScope.launch {
                    _events.send(ShowClientDeleteDialogEvent(intent.clientName))
                }
            }

            ConfirmClientDeleteIntent -> confirmDelete()

            AddClientClickedIntent -> {
                viewModelScope.launch {
                    _events.send(NavigateToAddEditClientEvent(clientId = null))
                }
            }
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

    private fun confirmDelete() {
        val clientId = pendingDeleteClientId ?: return

        viewModelScope.launch {
            clientDao.getClientById(clientId)?.let { client ->
                clientDao.deleteClient(client)
            }
            pendingDeleteClientId = null
        }
    }
}
