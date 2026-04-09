package com.avetiso.core

object AppConstants {

    // Константы базы данных
    internal object Data {
        const val DATABASE_NAME = "go_service_db"
        const val TABLE_APPOINTMENTS = "appointments"
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_CLIENTS = "clients"
        const val TABLE_SERVICES = "services"
        const val TABLE_TIMESLOT = "time_slots"
        const val DATA_STORE_NAME = "settings"
    }

    // Форматирование строк и префиксы
    object Format {
        const val INSTAGRAM_PREFIX = "@"
        const val FULL_DATE_FORMAT = "yyyy-MM-dd"
        const val DATE_FORMAT_HEADER = "d MMMM yyyy"
        const val DATE_FORMAT_YEAR_MONTH = "yyyy-MM"
        const val TIME_HH_MM = "%02d:%02d"
        const val PRICE_2_DECIMALS = "%.2f"
        const val DURATION = "%d ч %02d мин"
    }

    // Deep Links (используются внутри модуля или навигатором)
    object DeepLinks {
        const val CLIENTS_ADD = "goservice://clients/add"
        const val CLIENTS_EDIT = "goservice://clients/edit/"
        const val SIDEBAR_SETTINGS = "goservice://sidebar/settings"
        const val SIDEBAR_ABOUT = "goservice://sidebar/about"
        const val SCHEDULE_MAIN = "goservice://schedule/main"
        const val SCHEDULE_ADD_EDIT = "goservice://schedule/add_edit"
    }

    // Ключи результатов (Result Keys) внутри Bundle
    object Result {
        const val SELECTED_CLIENT = "selected_client"
        const val RESULT_CONFIRMED = "result_confirmed"
        const val DELETE_CONFIRMED = "delete_confirmed"
        const val DELETE_DIALOG = "delete_dialog"
        const val INPUT_DIALOG = "input_dialog"
        const val RESULT_TEXT = "result_text"

    }

    object Requests {
        const val CLIENT_SELECT = "client_selection_request"
        const val CHANGE_CURRENCY = "change_currency_request"
        const val ADD_CUSTOM_CURRENCY = "add_custom_currency_request"
    }

    object Preferences {
        const val KEY_DEFAULT_CURRENCY = "default_currency"
        const val KEY_CUSTOM_CURRENCIES = "custom_currencies"
    }

    // Временные интервалы и тайм-ауты
    object Time {
        const val SEARCH_DEBOUNCE = 300L
        const val SWIPE_REMOVE = 250L
        const val KEY_PENDING_DESTINATION = "key_pending_destination"
    }

    // UI: Анимации, прозрачность, задержки визуальных эффектов
    object Ui {
        const val ALPHA_DIMMED = 0.2f
        const val ALPHA_OPAQUE = 1.0f

        // Задержка перед запуском бегущей строки (Marquee)
        const val MARQUEE_START_DELAY = 2000L
        const val SNACKBAR_LONG_DURATION = 5000L
    }

    object Tag {
        const val DATE_PICKER = "DATE_PICKER"
    }

    // Управление отображением виджетами в модуле статистики
    object FeatureToggles {
        const val IS_INVENTORY_WIDGET_ENABLED = true // Управление отображением виджета склада
    }

    const val ID_NONE = -1L
}