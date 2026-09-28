package com.avetiso.feature_appointments.mvi

import androidx.annotation.StringRes

data class ShowAppointmentsToastEvent(
    @StringRes val messageResId: Int
) : AppointmentsEvent
