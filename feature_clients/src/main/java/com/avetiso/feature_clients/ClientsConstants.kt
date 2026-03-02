package com.avetiso.feature_clients

internal object ClientsConstants {

    // Ключи аргументов (то, что кладем в Bundle при навигации)
    object Args {
        const val CLIENT_ID = "clientId"
    }

    // Ключи запросов (Request Keys) для Fragment Result API
    object Requests {
        // Запрос на обновление списка после редактирования
        const val CLIENT_UPDATED = "client_updated_request"

        // Запрос на подтверждение удаления клиента
        const val CLIENT_DELETE = "client_delete_request"

        // Запрос на удаление из селектора
        const val CLIENT_DELETE_FROM_SELECTOR = "client_delete_selector_request"

        // Слушаем результат ввода текста (например, заметка)
        const val INPUT_FIELD = "input_field_request"
    }

    // Ключи результатов (Result Keys) внутри Bundle
    object ResultKeys {
        // Флаг: были ли изменения
        const val IS_CLIENT_UPDATED = "is_client_updated"
    }
}