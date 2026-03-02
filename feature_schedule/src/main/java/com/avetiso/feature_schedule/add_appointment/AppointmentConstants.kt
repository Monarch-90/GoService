package com.avetiso.feature_schedule.add_appointment

internal object AppointmentConstants {

    object Result {
        const val SERVICE_UPDATED = "service_updated"
        const val SELECTED_CATEGORY_NAME = "selected_category_name"
    }

    object Request {
        const val DELETE_SERVICE = "delete_service_request"
        const val DELETE_CATEGORY = "delete_category_request"
        const val DELETE_TIMESLOT = "delete_timeslot_request"
        const val SELECTION_CATEGORY = "category_selection_request"
        const val SET_DEFAULT_CURRENCY = "set_default_currency_request"
        const val DURATION_PICKER = "duration_picker_request"
        const val TIME_PICKER = "time_picker_request"
        const val INPUT_CATEGORY = "input_category_request"
    }

    object Tags {
        const val DURATION_PICKER_DIALOG = "tag_duration_picker_dialog"
        const val TIME_PICKER_DIALOG = "tag_time_picker_dialog"
    }
}