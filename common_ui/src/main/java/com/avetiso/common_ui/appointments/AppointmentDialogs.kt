package com.avetiso.common_ui.appointments

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.avetiso.common_ui.R
import com.avetiso.common_ui.compose_picker.ComposeDatePickerDialogFragment
import com.avetiso.common_ui.dialogs.DeleteDialogFragment
import com.avetiso.common_ui.dialogs.InputDialogFragment
import com.avetiso.core.AppConstants
import com.avetiso.core.entity.ui.Appointment
import com.avetiso.core.models.AppointmentStatus
import com.avetiso.core.utils.getStatusByIndex
import com.avetiso.core.utils.getStatusLabelsArray
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Fragment.showAppointmentStatusDialog(
    appointment: Appointment,
    rescheduleRequestKey: String,
    onStatusSelected: (AppointmentStatus) -> Unit
) {
    val statuses = requireContext().getStatusLabelsArray()
    val customTitleView = LayoutInflater.from(requireContext())
        .inflate(R.layout.status_dialog_title, null)

    val dialog = MaterialAlertDialogBuilder(requireContext())
        .setCustomTitle(customTitleView)
        .setItems(statuses) { _, which ->
            val selectedStatus = getStatusByIndex(which)
            if (selectedStatus == AppointmentStatus.RESCHEDULED) {
                ComposeDatePickerDialogFragment.newInstance(
                    requestKey = rescheduleRequestKey,
                    title = getString(R.string.Выберите_дату),
                    extraId = appointment.id
                ).show(childFragmentManager, AppConstants.Tag.DATE_PICKER)
            } else {
                onStatusSelected(selectedStatus)
            }
        }
        .create()

    dialog.setOnShowListener {
        dialog.window?.setBackgroundDrawableResource(com.avetiso.core.R.drawable.dialog_box_corners)
    }
    dialog.show()
}

fun Fragment.showAppointmentNoteDialog(
    currentNote: String,
    requestKey: String
) {
    InputDialogFragment.newInstance(
        requestKey = requestKey,
        title = getString(com.avetiso.core.R.string.Примечание),
        hint = getString(R.string.Введите_текст),
        initialValue = currentNote,
        isMultiline = true, // Включаем многострочный режим
        allowEmpty = true,
    ).show(childFragmentManager, AppConstants.Result.INPUT_DIALOG)
}

fun Fragment.showAppointmentDeleteDialog(
    requestKey: String
) {
    val messageText = getString(R.string.Удалить_запись)
    DeleteDialogFragment.newInstance(
        requestKey = requestKey,
        message = getString(com.avetiso.core.R.string.delete_dialog_message, messageText)
    ).show(childFragmentManager, AppConstants.Result.DELETE_DIALOG)
}