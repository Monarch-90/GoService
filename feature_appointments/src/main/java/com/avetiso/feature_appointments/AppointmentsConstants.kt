package com.avetiso.feature_appointments

internal object AppointmentsConstants {

    object ViewType {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
    }

    // Ключи запросов (Request Keys) для Fragment Result API
    object Requests {
        const val APPOINTMENT_DELETE = "appointments_delete_request"
        const val INPUT_NOTE_KEY = "appointments_input_note_request"
        const val RESCHEDULE_DATE_KEY = "appointments_reschedule_date_request"
    }

    object Pending {
        const val KEY_PENDING_DELETE_ID = "pending_delete_id"
        const val KEY_PENDING_NOTE_ID = "pending_note_id"
    }
}