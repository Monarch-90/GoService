package com.avetiso.feature_clients.add_edit.mvi

import com.avetiso.core.entity.ClientEntity

data class AddEditClientState(
    // Данные для полей ввода (черновик)
    val nameInput: String = "",
    val phoneInput: String = "",
    val instagramInput: String = "",
    val sourceInput: String = "",
    val discountInput: String = "",
    val noteInput: String = "",

    // Кастомные поля: Ключ = Название поля, Значение = Введенный текст
    val customFieldsInput: Map<String, String> = emptyMap(),

    // Служебные флаги
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val originalClient: ClientEntity? = null // Храним оригинал для сравнения при сохранении
)