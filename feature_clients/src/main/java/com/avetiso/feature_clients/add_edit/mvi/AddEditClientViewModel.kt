package com.avetiso.feature_clients.add_edit.mvi

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.avetiso.core.AppConstants
import com.avetiso.core.data.dao.ClientDao
import com.avetiso.core.entity.ClientEntity
import com.avetiso.core.models.UiText
import com.avetiso.feature_clients.ClientsConstants
import com.avetiso.feature_clients.R
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

    init {
        // Получаем клиента для редактирования из аргументов навигации
        val clientId: Long = savedStateHandle.get<Long>(ClientsConstants.Args.CLIENT_ID) ?: AppConstants.ID_NONE

        if (clientId != AppConstants.ID_NONE) {
            loadClient(clientId)
        }
    }

    fun onSaveClicked() {
        val currentState = _state.value
        val name = currentState.nameInput.trim()
        val prefix = AppConstants.Format.INSTAGRAM_PREFIX

        if (name.isBlank()) {
            // Можно добавить событие валидации, если нужно подсветить поле
            return
        }

        val phone = currentState.phoneInput.trim()
        val rawInstagram = currentState.instagramInput.trim()
        val finalInstagram = if (rawInstagram.isNotEmpty()) "$prefix$rawInstagram" else ""

        val clientToSave = ClientEntity(
            id = currentState.originalClient?.id ?: 0L,
            name = name,
            phoneNumber = phone,
            instagram = finalInstagram,
            source = currentState.sourceInput.trim(),
            discount = currentState.discountInput.toIntOrNull() ?: 0,
            note = currentState.noteInput.trim(),
            customFields = currentState.customFieldsInput
        )

        saveClientToDb(clientToSave)
    }

    private fun saveClientToDb(client: ClientEntity) {
        viewModelScope.launch {
            val idToExclude = client.id

            // Проверка на дубликаты (Ваша логика)
            val isDuplicate = when {
                client.name.isNotBlank() && client.phoneNumber.isNotBlank() &&
                        clientDao.findByNameAndPhone(client.name, client.phoneNumber, idToExclude) != null -> true

                client.name.isNotBlank() && client.instagram.isNotBlank() &&
                        clientDao.findByNameAndInstagram(client.name, client.instagram, idToExclude) != null -> true

                client.phoneNumber.isNotBlank() && client.instagram.isNotBlank() &&
                        clientDao.findByPhoneAndInstagram(client.phoneNumber, client.instagram, idToExclude) != null -> true

                else -> false
            }

            if (isDuplicate) {
                _eventChannel.send(
                    AddEditClientEvent.ShowToast(
                        UiText.StringResource(
                            R.string.Этот_клиент_уже_добавлен
                        )
                    )
                )
                return@launch
            }

            if (_state.value.isEditing) {
                clientDao.updateClient(client)
            } else {
                clientDao.insertClient(client)
            }
            _eventChannel.send(AddEditClientEvent.NavigateBackWithResult)
        }
    }

    private fun loadClient(clientId: Long) {
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            val client = clientDao.getClientById(clientId)
            if (client != null) {
                // Инициализируем поля ввода данными из БД
                _state.update {
                    it.copy(
                        isLoading = false,
                        isEditing = true,
                        originalClient = client,
                        nameInput = client.name,
                        phoneInput = client.phoneNumber,
                        instagramInput = client.instagram.removePrefix(AppConstants.Format.INSTAGRAM_PREFIX),
                        sourceInput = client.source,
                        discountInput = if (client.discount > 0) client.discount.toString() else "",
                        noteInput = client.note,
                        customFieldsInput = client.customFields
                    )
                }
            } else {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onNameChanged(text: String) {
        _state.update { it.copy(nameInput = text) }
    }

    fun onPhoneChanged(text: String) {
        _state.update { it.copy(phoneInput = text) }
    }

    fun onInstagramChanged(text: String) {
        _state.update { it.copy(instagramInput = text) }
    }

    fun onSourceChanged(text: String) {
        _state.update { it.copy(sourceInput = text) }
    }

    fun onDiscountChanged(text: String) {
        _state.update { it.copy(discountInput = text) }
    }

    fun onNoteChanged(text: String) {
        _state.update { it.copy(noteInput = text) }
    }

    // Добавление нового кастомного поля (пустое значение)
    fun addCustomField(fieldName: String) {
        _state.update {
            val newMap = it.customFieldsInput.toMutableMap()
            // Если поля еще нет, добавляем его
            if (!newMap.containsKey(fieldName)) {
                newMap[fieldName] = ""
            }
            it.copy(customFieldsInput = newMap)
        }
    }

    // Удаление кастомного поля
    fun removeCustomField(fieldName: String) {
        _state.update {
            val newMap = it.customFieldsInput.toMutableMap()
            newMap.remove(fieldName)
            it.copy(customFieldsInput = newMap)
        }
    }

    // Изменение текста в кастомном поле
    fun onCustomFieldValueChanged(fieldName: String, value: String) {
        _state.update {
            val newMap = it.customFieldsInput.toMutableMap()
            newMap[fieldName] = value
            it.copy(customFieldsInput = newMap)
        }
    }
}