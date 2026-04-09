package com.avetiso.core.models

enum class AppCurrency {
    GEL,
    USD,
    EUR;

    companion object {
        /**
         * Получить список всех кодов валют в виде строк.
         * Используется для инициализации Spinner (выпадающего списка).
         * Результат: ["BYN", "USD", "EUR", ...]
         */
        fun getCodesList(): List<String> = entries.map { it.name }

        /**
         * Безопасное получение валюты из строки (например, из БД или SharedPreferences).
         * Если пришла неизвестная строка (null или мусор), возвращаем значение по умолчанию (например, BYN).
         * Это спасает приложение от краша.
         */
        fun fromCode(code: String?): AppCurrency {
            return entries.find { it.name == code } ?: GEL
        }
    }
}