package com.avetiso.common_ui.dialogs.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConfirmationDialogPayload(
    val requestKey: String,
    val title: String,
    val message: String,
    val positiveText: String? = null,
    val negativeText: String? = null,
    val isDestructive: Boolean = false
) : Parcelable