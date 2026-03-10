package com.avetiso.feature_schedule

internal object ScheduleConstants {

    // Аргументы (Bundle)
    object Args {
        const val APPOINTMENT_ID = "appointmentId"
        const val SELECTED_DATE = "selectedDate"
    }

    // Ключи запросов (Fragment Result API)
    object Requests {
        const val APPOINTMENT_DELETE = "appointment_delete_request"
        const val INPUT_NOTE_KEY = "input_note_request"
        const val RESCHEDULE_DATE_KEY = "reschedule_date_request"
    }
}