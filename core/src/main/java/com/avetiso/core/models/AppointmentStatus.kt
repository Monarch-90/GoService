package com.avetiso.core.models

enum class AppointmentStatus(val key: String) {
    ACTIVE("status_active"),
    COMPLETED("status_completed"),
    CANCELLED("status_cancelled"),
    RESCHEDULED("status_reschedule"),
    NO_SHOW("status_no_show");

    companion object {
        fun fromKey(key: String): AppointmentStatus {
            return entries.find { it.key == key } ?: ACTIVE
        }
    }
}