package com.avetiso.feature_schedule.add_appointment.steps.step3.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.entity.ClientEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Step3SelectClientViewModel @Inject constructor(
    private val clientDao: ClientDao,
) : ViewModel() {

    private val _state = MutableStateFlow(Step3State())

    // Этот Flow будет переизлучать список клиентов при изменении searchQuery
    val clients = _state
        .debounce(300)
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

    fun deleteClient(client: ClientEntity) {
        viewModelScope.launch {
            clientDao.deleteClient(client)
        }
    }
}