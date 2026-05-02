package com.avetiso.feature_sidebar.contact_developers.mvi

/**
 * Типы ошибок валидации полей ввода.
 * Чистая бизнес-логика, не зависящая от Android SDK.
 */
enum class ContactDevelopersFieldError {
    EMPTY_FIELD,
    INVALID_EMAIL
}