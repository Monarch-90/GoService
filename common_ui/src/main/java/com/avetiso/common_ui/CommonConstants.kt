package com.avetiso.common_ui

object CommonConstants {

    object ConfirmationDialogKeys {
        const val TAG = "ConfirmationDialogFragment"
        const val ARG_PAYLOAD = "arg_confirmation_payload"
    }

    // Группировка ключей возвращаемых результатов (Result Keys)
    object Result {
        // Single Date Picker
        const val DATE = "result_date"
        const val DATE_EXTRA_ID = "result_date_extra_id"

        // Date Range Picker
        const val START_DATE = "result_start_date"
        const val END_DATE = "result_end_date"

        const val HOUR = "result_hour"
        const val MINUTE = "result_minute"
        const val TIME_EXTRA_ID = "result_time_extra_id"
    }

    // Группировка ключей запросов (Request Keys) для Fragment Result API
    object Request {
        const val DELETE_REQUEST = "delete_request"
        const val DATE_PICKER = "date_picker_request"
        const val DATE_RANGE_PICKER = "date_range_picker_request"
        const val TIME_PICKER = "time_picker_request"
    }

    // Универсальные аргументы для передачи во фрагменты (Arguments)
    object Args {
        const val TITLE = "arg_title"
        const val INITIAL_DATE = "arg_initial_date"
        const val REQUEST_KEY = "arg_request_key"
        const val EXTRA_ID = "arg_extra_id"
        const val INITIAL_HOUR = "arg_initial_hour"
        const val INITIAL_MINUTE = "arg_initial_minute"
        const val NO_ID = -1L
    }
}