package com.avetiso.feature_schedule

internal object ScheduleConstants {

    object ViewType {
        const val APPOINTMENT = 0
        const val BANNER_AD = 1
    }

    object Args {
        const val APPOINTMENT_ID = "appointmentId"
        const val SELECTED_DATE = "selectedDate"
    }

    object Requests {
        const val APPOINTMENT_DELETE = "appointment_delete_request"
        const val INPUT_NOTE_KEY = "input_note_request"
        const val RESCHEDULE_DATE_KEY = "reschedule_date_request"
    }

    object Pending {
        const val KEY_SCHEDULE_PENDING_DELETE_ID = "schedule_pending_delete_id"
        const val KEY_SCHEDULE_PENDING_NOTE_ID = "schedule_pending_note_id"
    }
}
