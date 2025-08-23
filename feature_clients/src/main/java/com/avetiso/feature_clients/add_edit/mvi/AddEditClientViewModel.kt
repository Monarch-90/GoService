package com.avetiso.feature_clients.add_edit.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.entity.ClientEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditClientViewModel @Inject constructor(
    private val clientDao: ClientDao,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val _state = MutableStateFlow(AddEditClientState())
    val state = _state.asStateFlow()

    init {
        // Получаем клиента для редактирования из аргументов навигации
        val client: ClientEntity? = savedStateHandle["clientToEdit"]
        if (client != null) {
            _state.update { it.copy(client = client, isEditing = true) }
        }
    }

    fun saveClient(client: ClientEntity) {
        viewModelScope.launch {
            if (_state.value.isEditing) {
                clientDao.updateClient(client)
            } else {
                clientDao.insertClient(client)
            }
            // Устанавливаем флаг для навигации назад
            _state.update { it.copy(navigateBack = true) }
        }
    }
}