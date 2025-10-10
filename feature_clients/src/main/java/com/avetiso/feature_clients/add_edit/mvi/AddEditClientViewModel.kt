package com.avetiso.feature_clients.add_edit.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.entity.ClientEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
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

    private val _eventChannel = Channel<AddEditClientEvent>()
    val events = _eventChannel.receiveAsFlow()

    private val message = "Этот клиент уже добавлен"

    init {
        // Получаем клиента для редактирования из аргументов навигации
        val clientId: Long = savedStateHandle.get<Long>("clientId") ?: -1L

        if (clientId != -1L) {
            // Если это не новый клиент, запускаем загрузку из БД
            viewModelScope.launch {
                val client = clientDao.getClientById(clientId)
                if (client != null) {
                    _state.update { it.copy(client = client, isEditing = true) }
                }
            }
        }
    }

    fun saveClient(client: ClientEntity) {
        viewModelScope.launch {
            val name = client.name.trim()
            val phone = client.phoneNumber.trim()
            val instagram = client.instagram.trim()
            val idToExclude = client.id

            val isDuplicate = when {
                // Проверка 1: Имя + Телефон
                name.isNotBlank() && phone.isNotBlank() &&
                        clientDao.findByNameAndPhone(name, phone, idToExclude) != null -> true

                // Проверка 2: Имя + Инстаграм
                name.isNotBlank() && instagram.isNotBlank() &&
                        clientDao.findByNameAndInstagram(name, instagram, idToExclude) != null -> true

                // Проверка 3: Телефон + Инстаграм
                phone.isNotBlank() && instagram.isNotBlank() &&
                        clientDao.findByPhoneAndInstagram(phone, instagram, idToExclude) != null -> true

                // Если ни одно из условий не сработало
                else -> false
            }

            if (isDuplicate) {
                _eventChannel.send(AddEditClientEvent.ShowToast(message))
                return@launch
            }

            // Если все проверки пройдены, сохраняем
            if (_state.value.isEditing) {
                clientDao.updateClient(client)
            } else {
                clientDao.insertClient(client)
            }
            // Отправляем событие для навигации назад
            _eventChannel.send(AddEditClientEvent.NavigateBackWithResult)
        }
    }

    fun handleViewEvent(event: AddEditClientEvent) {
        when (event) {
            is AddEditClientEvent.InitialDataSet -> {
                _state.update { it.copy(isInitialDataSet = true) }
            }
            // Другие события от View можно будет добавлять сюда
            else -> {
                // Игнорируем события, которые ViewModel сам отправляет (ShowToast и т.д.)
            }
        }
    }
}